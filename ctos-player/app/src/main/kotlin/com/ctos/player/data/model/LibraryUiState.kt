package com.ctos.player.data.model

/** Chips of the library filter row. */
enum class LibraryFilter(val label: String) {
    All("Todas"),
    Albums("Álbumes"),
    Artists("Artistas"),
    Playlists("Playlists"),
    Videos("Videos"),
}

/** Library loading lifecycle, used to swap skeletons for real content. */
sealed interface LibraryUiState {
    data object Loading : LibraryUiState
    data object PermissionRequired : LibraryUiState
    data class Ready(val songs: List<Song>) : LibraryUiState
}
