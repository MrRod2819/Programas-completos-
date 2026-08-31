package com.ctos.player.ui.screens

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.ctos.player.core.theme.LocalPalette
import com.ctos.player.data.model.PlayerState
import com.ctos.player.data.model.Song
import com.ctos.player.ui.components.SegmentedControl
import com.ctos.player.ui.components.SheetHandle
import com.ctos.player.ui.components.TrackRow
import com.ctos.player.ui.components.staggeredEntry

/**
 * Draggable queue sheet with the "Ahora / A continuación / Historial"
 * segments. Long-pressing a drag handle and moving vertically reorders the
 * queue; rows animate to their new slot with a soft spring.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(
    state: PlayerState,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xE6121218),
        contentColor = palette.textPrimary,
        dragHandle = {
            Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                SheetHandle()
            }
        },
        modifier = modifier,
    ) {
        var segment by remember { mutableIntStateOf(1) }
        val songs = when (segment) {
            0 -> listOfNotNull(state.current)
            1 -> state.upNext
            else -> state.history
        }
        val offset = if (segment == 1 && state.queueIndex >= 0) state.queueIndex else 0

        Column(Modifier.padding(horizontal = 18.dp)) {
            SegmentedControl(
                options = listOf("Ahora", "A continuación", "Historial"),
                selectedIndex = segment,
                onSelect = { segment = it },
            )
            QueueList(
                songs = songs,
                activeSongId = state.current?.id,
                reorderable = segment == 1,
                onSelect = { index -> onSelect(index + offset) },
                onMove = { from, to -> onMove(from + offset, to + offset) },
                modifier = Modifier.padding(top = 14.dp),
            )
        }
    }
}

@Composable
private fun QueueList(
    songs: List<Song>,
    activeSongId: Long?,
    reorderable: Boolean,
    onSelect: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalPalette.current
    val listState = rememberLazyListState()
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragAccumulator by remember { mutableStateOf(0f) }

    if (songs.isEmpty()) {
        Text(
            "La cola está vacía.",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textMuted,
            modifier = modifier.padding(vertical = 24.dp),
        )
        return
    }

    LazyColumn(state = listState, modifier = modifier) {
        itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
            TrackRow(
                song = song,
                onClick = { onSelect(index) },
                isActive = song.id == activeSongId,
                showMenu = false,
                modifier = Modifier
                    .animateItem()
                    .staggeredEntry(index = index.coerceAtMost(8), stepMillis = 30),
                leading = if (!reorderable) null else {
                    {
                        Icon(
                            imageVector = Icons.Filled.DragHandle,
                            contentDescription = "Reordenar",
                            tint = palette.textMuted,
                            modifier = Modifier
                                .size(16.dp)
                                .pointerInput(index, songs.size) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggingIndex = index
                                            dragAccumulator = 0f
                                        },
                                        onDragEnd = {
                                            draggingIndex = null
                                            dragAccumulator = 0f
                                        },
                                        onDragCancel = {
                                            draggingIndex = null
                                            dragAccumulator = 0f
                                        },
                                    ) { change, dragAmount ->
                                        change.consume()
                                        dragAccumulator += dragAmount.y
                                        val rowHeight = ROW_HEIGHT_DP.dp.toPx()
                                        val from = draggingIndex ?: return@detectDragGesturesAfterLongPress
                                        val steps = (dragAccumulator / rowHeight).toInt()
                                        if (steps != 0) {
                                            val to = (from + steps).coerceIn(songs.indices)
                                            if (to != from) {
                                                onMove(from, to)
                                                draggingIndex = to
                                                dragAccumulator -= steps * rowHeight
                                            }
                                        }
                                    }
                                },
                        )
                    }
                },
            )
        }
        item { Box(Modifier.height(24.dp)) }
    }
}

private const val ROW_HEIGHT_DP = 64
