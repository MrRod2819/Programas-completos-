package com.ctos.player.data.model

/** Repeat behaviour exposed by the Now Playing repeat toggle. */
enum class RepeatMode { Off, All, One }

/** Snapshot of everything the UI needs to render the player. */
data class PlayerState(
    val current: Song? = null,
    val queue: List<Song> = emptyList(),
    val queueIndex: Int = -1,
    val history: List<Song> = emptyList(),
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.Off,
    val favorites: Set<Long> = emptySet(),
    val sourceLabel: String = "Frecuencia Nocturna",
) {
    val progress: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    val remainingMs: Long get() = (durationMs - positionMs).coerceAtLeast(0L)

    /** Tracks after the current one — the "A continuación" segment. */
    val upNext: List<Song>
        get() = if (queueIndex in queue.indices) queue.drop(queueIndex) else queue
}
