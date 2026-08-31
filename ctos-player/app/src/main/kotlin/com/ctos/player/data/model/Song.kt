package com.ctos.player.data.model

import android.net.Uri

/** A single playable item coming from the device [android.provider.MediaStore]. */
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val uri: Uri,
    val albumArtUri: Uri?,
    val mimeType: String,
    val year: Int,
    val dateAdded: Long,
) {
    val isVideoLike: Boolean get() = mimeType.startsWith("video")

    /** `3:24` style duration used across the track rows. */
    val durationLabel: String get() = formatDuration(durationMs)
}

fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

/** `01:24` style readout used by the Now Playing timers. */
fun formatClock(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
