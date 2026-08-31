package com.ctos.player.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ctos.player.CtosApp
import com.ctos.player.data.model.LibraryFilter
import com.ctos.player.data.model.LibraryUiState
import com.ctos.player.data.model.PlayerState
import com.ctos.player.data.model.RingStyle
import com.ctos.player.data.model.Song
import com.ctos.player.data.model.ThemePalette
import com.ctos.player.service.PlaybackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Search results grouped the way the radar screen displays them. */
data class SearchResults(
    val songs: List<Song> = emptyList(),
    val artists: List<String> = emptyList(),
    val videos: List<Song> = emptyList(),
) {
    val isEmpty: Boolean get() = songs.isEmpty() && artists.isEmpty() && videos.isEmpty()
}

/** Aggregated stats rendered by the profile screen. */
data class OperatorStats(
    val totalMinutes: Long,
    val topGenreLabel: String,
    val playlists: Int,
    val streakDays: Int,
    val level: Int,
    val xp: Int,
) {
    val xpProgress: Float get() = (xp % XP_PER_LEVEL) / XP_PER_LEVEL.toFloat()

    companion object {
        const val XP_PER_LEVEL = 500
    }
}

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val mediaRepository = (app as CtosApp).mediaRepository
    private val settingsRepository = (app as CtosApp).settingsRepository
    private val playback = PlaybackManager(app, viewModelScope)

    private val _library = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val library: StateFlow<LibraryUiState> = _library.asStateFlow()

    private val _filter = MutableStateFlow(LibraryFilter.All)
    val filter: StateFlow<LibraryFilter> = _filter.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _history = MutableStateFlow<List<Song>>(emptyList())

    val palette: StateFlow<ThemePalette> = settingsRepository.palette
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemePalette.DedSecPop)

    val ringStyle: StateFlow<RingStyle> = settingsRepository.ringStyle
        .stateIn(viewModelScope, SharingStarted.Eagerly, RingStyle.GraffitiSplash)

    /** Everything the player UI needs, derived from the Media3 snapshot. */
    val playerState: StateFlow<PlayerState> = combine(
        playback.snapshot,
        _library,
        _history,
        settingsRepository.favorites,
    ) { snapshot, library, history, favorites ->
        val songs = (library as? LibraryUiState.Ready)?.songs.orEmpty()
        val byId = songs.associateBy(Song::id)
        val queue = snapshot.queueIds.mapNotNull(byId::get)
        PlayerState(
            current = snapshot.currentId?.let(byId::get),
            queue = queue,
            queueIndex = snapshot.queueIndex,
            history = history,
            isPlaying = snapshot.isPlaying,
            positionMs = snapshot.positionMs,
            durationMs = snapshot.durationMs.takeIf { it > 0 }
                ?: snapshot.currentId?.let { byId[it]?.durationMs } ?: 0L,
            shuffle = snapshot.shuffle,
            repeatMode = snapshot.repeatMode,
            favorites = favorites,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlayerState())

    init {
        playback.connect()
    }

    /** Called once the runtime media permission has been granted. */
    fun loadLibrary() {
        viewModelScope.launch {
            _library.value = LibraryUiState.Loading
            _library.value = LibraryUiState.Ready(mediaRepository.loadSongs())
        }
    }

    fun onPermissionDenied() {
        _library.value = LibraryUiState.PermissionRequired
    }

    fun setFilter(filter: LibraryFilter) {
        _filter.value = filter
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    private val songs: List<Song>
        get() = (_library.value as? LibraryUiState.Ready)?.songs.orEmpty()

    /** Library content after applying the selected chip. */
    fun filteredSongs(): List<Song> = when (_filter.value) {
        LibraryFilter.All -> songs
        LibraryFilter.Albums -> songs.sortedBy { it.album }
        LibraryFilter.Artists -> songs.sortedBy { it.artist }
        LibraryFilter.Playlists -> songs.filter { it.id in playerState.value.favorites }
        LibraryFilter.Videos -> songs.filter(Song::isVideoLike)
    }

    fun recentlyPlayed(): List<Song> = (_history.value + songs).distinctBy(Song::id).take(10)

    fun search(query: String): SearchResults {
        if (query.isBlank()) return SearchResults()
        val needle = query.trim().lowercase()
        val matches = songs.filter {
            it.title.lowercase().contains(needle) ||
                it.artist.lowercase().contains(needle) ||
                it.album.lowercase().contains(needle)
        }
        return SearchResults(
            songs = matches.filterNot(Song::isVideoLike),
            artists = matches.map(Song::artist).distinct(),
            videos = matches.filter(Song::isVideoLike),
        )
    }

    fun stats(): OperatorStats {
        val totalMinutes = songs.sumOf { it.durationMs } / 60_000
        val topGenre = songs.groupingBy { it.album.ifBlank { "Synthwave" } }
            .eachCount()
            .maxByOrNull { it.value }?.key ?: "Synthwave"
        val xp = (_history.value.size * 40) + songs.size * 10
        return OperatorStats(
            totalMinutes = totalMinutes,
            topGenreLabel = topGenre,
            playlists = playerState.value.favorites.size,
            streakDays = (_history.value.size % 7) + 1,
            level = xp / OperatorStats.XP_PER_LEVEL + 1,
            xp = xp,
        )
    }

    fun play(song: Song, queue: List<Song> = filteredSongs()) {
        val list = queue.ifEmpty { listOf(song) }
        val index = list.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        playback.setQueue(list, index)
        _history.value = (listOf(song) + _history.value).distinctBy(Song::id).take(50)
    }

    fun playPause() = playback.playPause()
    fun next() = playback.next()
    fun previous() = playback.previous()
    fun seekTo(positionMs: Long) = playback.seekTo(positionMs)
    fun seekToQueueIndex(index: Int) = playback.seekToQueueIndex(index)
    fun moveQueueItem(from: Int, to: Int) = playback.moveQueueItem(from, to)
    fun removeQueueItem(index: Int) = playback.removeQueueItem(index)
    fun addToQueue(song: Song) = playback.addToQueue(song)
    fun toggleShuffle() = playback.toggleShuffle()
    fun cycleRepeatMode() = playback.cycleRepeatMode()

    fun selectPalette(palette: ThemePalette) {
        viewModelScope.launch { settingsRepository.setPalette(palette) }
    }

    fun selectRingStyle(style: RingStyle) {
        viewModelScope.launch { settingsRepository.setRingStyle(style) }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch { settingsRepository.toggleFavorite(song.id) }
    }

    override fun onCleared() {
        playback.release()
        super.onCleared()
    }
}
