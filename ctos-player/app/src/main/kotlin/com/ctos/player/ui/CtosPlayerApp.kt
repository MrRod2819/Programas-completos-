package com.ctos.player.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ctos.player.core.theme.CyberpunkSurface
import com.ctos.player.data.model.LibraryUiState
import com.ctos.player.data.model.Song
import com.ctos.player.ui.components.MiniPlayer
import com.ctos.player.ui.nav.CtosBottomBar
import com.ctos.player.ui.nav.CtosDestination
import com.ctos.player.ui.screens.HomeScreen
import com.ctos.player.ui.screens.NowPlayingScreen
import com.ctos.player.ui.screens.ProfileScreen
import com.ctos.player.ui.screens.QueueSheet
import com.ctos.player.ui.screens.SearchScreen
import com.ctos.player.ui.screens.SettingsScreen
import com.ctos.player.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val OPERATOR_NAME = "Reina_0x7"

/** Root of the UI: bottom navigation, mini player and the full-screen player. */
@Composable
fun CtosPlayerApp(viewModel: PlayerViewModel, modifier: Modifier = Modifier) {
    val library by viewModel.library.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val palette by viewModel.palette.collectAsStateWithLifecycle()
    val ringStyle by viewModel.ringStyle.collectAsStateWithLifecycle()

    var destination by remember { mutableStateOf(CtosDestination.Home) }
    var showNowPlaying by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    val clock = rememberClock()

    // The radar keeps scanning briefly after each keystroke.
    var scanning by remember { mutableStateOf(false) }
    LaunchedEffect(query) {
        if (query.isNotBlank()) {
            scanning = true
            delay(700)
        }
        scanning = false
    }

    BackHandler(enabled = showNowPlaying) { showNowPlaying = false }

    CyberpunkSurface(modifier) {
        Box(Modifier.fillMaxSize().systemBarsPadding()) {
            when (destination) {
                CtosDestination.Home, CtosDestination.Library, CtosDestination.Radio -> HomeScreen(
                    state = library,
                    songs = viewModel.filteredSongs(),
                    recent = viewModel.recentlyPlayed(),
                    filter = filter,
                    activeSongId = playerState.current?.id,
                    operatorName = OPERATOR_NAME,
                    clock = clock,
                    onFilterChange = viewModel::setFilter,
                    onSongClick = { song -> viewModel.play(song) },
                    onSongMenu = viewModel::addToQueue,
                    onSearchClick = { destination = CtosDestination.Search },
                    onTabSelected = { index ->
                        destination = when (index) {
                            1 -> CtosDestination.Search
                            2 -> CtosDestination.Library
                            3 -> CtosDestination.Radio
                            4 -> CtosDestination.Profile
                            else -> CtosDestination.Home
                        }
                    },
                    onThemeClick = { destination = CtosDestination.Settings },
                )

                CtosDestination.Search -> SearchScreen(
                    query = query,
                    results = viewModel.search(query),
                    scanning = scanning,
                    clock = clock,
                    onQueryChange = viewModel::setQuery,
                    onSongClick = { song -> viewModel.play(song, viewModel.search(query).songs) },
                )

                CtosDestination.Profile -> ProfileScreen(
                    operatorName = OPERATOR_NAME,
                    stats = viewModel.stats(),
                    recentActivity = viewModel.recentlyPlayed(),
                    clock = clock,
                    onSongClick = { song -> viewModel.play(song) },
                )

                CtosDestination.Settings -> SettingsScreen(
                    selectedPalette = palette,
                    selectedRing = ringStyle,
                    clock = clock,
                    onSelectPalette = viewModel::selectPalette,
                    onSelectRing = viewModel::selectRingStyle,
                )
            }

            val current: Song? = playerState.current
            if (current != null) {
                MiniPlayer(
                    song = current,
                    isPlaying = playerState.isPlaying,
                    progress = playerState.progress,
                    onPlayPause = viewModel::playPause,
                    onExpand = { showNowPlaying = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 10.dp, end = 10.dp, bottom = 74.dp),
                )
            }

            CtosBottomBar(
                selected = destination,
                onSelect = { destination = it },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        AnimatedVisibility(
            visible = showNowPlaying && playerState.current != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            NowPlayingScreen(
                state = playerState,
                clock = clock,
                onCollapse = { showNowPlaying = false },
                onPlayPause = viewModel::playPause,
                onNext = { viewModel.next() },
                onPrevious = viewModel::previous,
                onSeek = viewModel::seekTo,
                onShuffle = viewModel::toggleShuffle,
                onRepeat = viewModel::cycleRepeatMode,
                onToggleFavorite = { playerState.current?.let(viewModel::toggleFavorite) },
                onQueue = { showQueue = true },
                modifier = Modifier.systemBarsPadding(),
            )
        }

        if (showQueue) {
            QueueSheet(
                state = playerState,
                onDismiss = { showQueue = false },
                onSelect = viewModel::seekToQueueIndex,
                onMove = viewModel::moveQueueItem,
            )
        }
    }
}

/** Live `HH:mm` readout for the faux ctOS status bar. */
@Composable
private fun rememberClock(): String {
    var value by remember { mutableStateOf(currentTime()) }
    LaunchedEffect(Unit) {
        while (true) {
            value = currentTime()
            delay(10_000)
        }
    }
    return value
}

private fun currentTime(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

/** Padding that keeps list content clear of the mini player + bottom bar. */
val PlayerAwarePadding = PaddingValues(bottom = 150.dp)

@Suppress("unused")
private fun libraryIsReady(state: LibraryUiState) = state is LibraryUiState.Ready
