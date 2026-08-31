package com.ctos.player.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.ctos.player.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Scans the device with the scoped-storage friendly [MediaStore] API and maps
 * every audio entry to a [Song]. Clips shorter than [MIN_DURATION_MS] are
 * ignored so ringtones and notification blips never pollute the library.
 */
class MediaRepository(private val context: Context) {

    suspend fun loadSongs(): List<Song> = withContext(Dispatchers.IO) {
        val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATE_ADDED,
        )

        val selection = buildString {
            append("${MediaStore.Audio.Media.IS_MUSIC} != 0")
            append(" AND ${MediaStore.Audio.Media.DURATION} >= ?")
            append(" AND (")
            append(SUPPORTED_MIME_TYPES.joinToString(" OR ") { "${MediaStore.Audio.Media.MIME_TYPE} = ?" })
            append(")")
        }
        val selectionArgs = arrayOf(MIN_DURATION_MS.toString(), *SUPPORTED_MIME_TYPES)
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        val songs = mutableListOf<Song>()
        context.contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)
            ?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    songs += Song(
                        id = id,
                        title = cursor.getString(titleCol) ?: "Pista desconocida",
                        artist = cursor.getString(artistCol)?.takeIf { it != "<unknown>" } ?: "Artista desconocido",
                        album = cursor.getString(albumCol) ?: "",
                        durationMs = cursor.getLong(durationCol),
                        uri = ContentUris.withAppendedId(collection, id),
                        albumArtUri = albumArtUri(cursor.getLong(albumIdCol)),
                        mimeType = cursor.getString(mimeCol).orEmpty(),
                        year = cursor.getInt(yearCol),
                        dateAdded = cursor.getLong(dateCol),
                    )
                }
            }
        songs
    }

    private fun albumArtUri(albumId: Long): Uri? =
        if (albumId <= 0) null else ContentUris.withAppendedId(ALBUM_ART_BASE, albumId)

    private companion object {
        const val MIN_DURATION_MS = 30_000L
        val ALBUM_ART_BASE: Uri = Uri.parse("content://media/external/audio/albumart")
        val SUPPORTED_MIME_TYPES = arrayOf(
            "audio/mpeg",
            "audio/mp3",
            "audio/flac",
            "audio/x-flac",
            "audio/wav",
            "audio/x-wav",
            "audio/mp4",
            "audio/m4a",
            "audio/x-m4a",
        )
    }
}
