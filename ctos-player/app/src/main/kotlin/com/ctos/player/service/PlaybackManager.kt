package com.ctos.player.service

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.ctos.player.data.model.RepeatMode
import com.ctos.player.data.model.Song
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Everything the UI observes about the underlying Media3 controller. */
data class PlaybackSnapshot(
    val currentId: Long? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.Off,
    val queueIds: List<Long> = emptyList(),
    val queueIndex: Int = -1,
)

/**
 * Thin wrapper around a [MediaController] bound to [PlaybackService]. It keeps
 * a [StateFlow] of the playback snapshot so Compose can render the player
 * without ever touching the controller directly.
 */
class PlaybackManager(
    private val context: Context,
    private val scope: CoroutineScope,
) {
    private val _snapshot = MutableStateFlow(PlaybackSnapshot())
    val snapshot: StateFlow<PlaybackSnapshot> = _snapshot.asStateFlow()

    private var controller: MediaController? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = publish()
    }

    /** Binds to the playback service and starts the position ticker. */
    fun connect() {
        if (controller != null) return
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener(
            {
                controller = future.get().also { it.addListener(listener) }
                publish()
            },
            MoreExecutors.directExecutor(),
        )
        scope.launch {
            while (isActive) {
                delay(POSITION_POLL_MS)
                val player = controller
                if (player != null && player.isPlaying) publish()
            }
        }
    }

    fun release() {
        controller?.removeListener(listener)
        controller?.release()
        controller = null
    }

    /** Replaces the queue with [songs] and starts at [startIndex]. */
    fun setQueue(songs: List<Song>, startIndex: Int, play: Boolean = true) {
        val player = controller ?: return
        if (songs.isEmpty()) return
        player.setMediaItems(songs.map(::toMediaItem), startIndex.coerceIn(songs.indices), 0L)
        player.prepare()
        player.playWhenReady = play
        publish()
    }

    fun playPause() {
        val player = controller ?: return
        if (player.isPlaying) player.pause() else player.play()
        publish()
    }

    fun next() = controller?.let { if (it.hasNextMediaItem()) it.seekToNextMediaItem() }

    fun previous() {
        val player = controller ?: return
        // Mirrors the usual player behaviour: restart the track before skipping back.
        if (player.currentPosition > RESTART_THRESHOLD_MS || !player.hasPreviousMediaItem()) {
            player.seekTo(0L)
        } else {
            player.seekToPreviousMediaItem()
        }
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        publish()
    }

    fun seekToQueueIndex(index: Int) {
        val player = controller ?: return
        if (index in 0 until player.mediaItemCount) {
            player.seekTo(index, 0L)
            player.play()
        }
    }

    fun moveQueueItem(from: Int, to: Int) {
        val player = controller ?: return
        if (from in 0 until player.mediaItemCount && to in 0 until player.mediaItemCount) {
            player.moveMediaItem(from, to)
            publish()
        }
    }

    fun removeQueueItem(index: Int) {
        val player = controller ?: return
        if (index in 0 until player.mediaItemCount) {
            player.removeMediaItem(index)
            publish()
        }
    }

    fun addToQueue(song: Song) {
        controller?.addMediaItem(toMediaItem(song))
        publish()
    }

    fun toggleShuffle() {
        val player = controller ?: return
        player.shuffleModeEnabled = !player.shuffleModeEnabled
        publish()
    }

    fun cycleRepeatMode() {
        val player = controller ?: return
        player.repeatMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        publish()
    }

    private fun publish() {
        val player = controller ?: return
        _snapshot.value = PlaybackSnapshot(
            currentId = player.currentMediaItem?.mediaId?.toLongOrNull(),
            isPlaying = player.isPlaying,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.takeIf { it > 0 } ?: 0L,
            shuffle = player.shuffleModeEnabled,
            repeatMode = when (player.repeatMode) {
                Player.REPEAT_MODE_ONE -> RepeatMode.One
                Player.REPEAT_MODE_ALL -> RepeatMode.All
                else -> RepeatMode.Off
            },
            queueIds = (0 until player.mediaItemCount).map {
                player.getMediaItemAt(it).mediaId.toLongOrNull() ?: -1L
            },
            queueIndex = player.currentMediaItemIndex,
        )
    }

    private fun toMediaItem(song: Song): MediaItem = MediaItem.Builder()
        .setMediaId(song.id.toString())
        .setUri(song.uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setAlbumTitle(song.album)
                .setArtworkUri(song.albumArtUri)
                .setIsBrowsable(false)
                .setIsPlayable(true)
                .build(),
        )
        .build()

    private companion object {
        const val POSITION_POLL_MS = 500L
        const val RESTART_THRESHOLD_MS = 3_000L
    }
}
