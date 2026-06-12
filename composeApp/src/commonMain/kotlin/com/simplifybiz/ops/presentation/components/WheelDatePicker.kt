package com.simplifybiz.ops.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val MONTHS = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

private fun daysIn(month: Int, year: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    else -> if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) 29 else 28
}

/**
 * UIDatePicker(wheels)-style date picker presented as a bottom sheet,
 * matching what iOS users expect instead of the Material calendar grid.
 * Pure Compose (LazyColumn + snap fling) — no cinterop, no new deps.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelDatePickerSheet(
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    val years = remember { (today.year..today.year + 5).toList() }

    var monthIdx by remember { mutableStateOf(today.monthNumber - 1) }
    var yearIdx by remember { mutableStateOf(0) }
    var dayIdx by remember { mutableStateOf(today.dayOfMonth - 1) }

    val dayCount = daysIn(monthIdx + 1, years[yearIdx])
    if (dayIdx >= dayCount) dayIdx = dayCount - 1

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(Modifier.weight(1f))
                Text("Due date", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = {
                    onConfirm(LocalDate(years[yearIdx], monthIdx + 1, dayIdx + 1))
                }) { Text("Done", fontWeight = FontWeight.SemiBold) }
            }

            Box {
                // Center selection band behind the wheels
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(WHEEL_ITEM_HEIGHT)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Row(Modifier.fillMaxWidth()) {
                    Wheel(
                        items = MONTHS,
                        selectedIndex = monthIdx,
                        onSelected = { monthIdx = it },
                        modifier = Modifier.weight(1.4f)
                    )
                    Wheel(
                        items = (1..dayCount).map { it.toString() },
                        selectedIndex = dayIdx,
                        onSelected = { dayIdx = it },
                        modifier = Modifier.weight(0.8f)
                    )
                    Wheel(
                        items = years.map { it.toString() },
                        selectedIndex = yearIdx,
                        onSelected = { yearIdx = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private val WHEEL_ITEM_HEIGHT = 36.dp
private const val VISIBLE_ROWS = 5

@Composable
private fun Wheel(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val state = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
    )

    // Report the row resting at the center whenever scrolling settles
    LaunchedEffect(state.isScrollInProgress, items.size) {
        if (!state.isScrollInProgress) {
            val idx = state.firstVisibleItemIndex.coerceIn(0, items.size - 1)
            onSelected(idx)
        }
    }

    LazyColumn(
        state = state,
        flingBehavior = rememberSnapFlingBehavior(lazyListState = state),
        contentPadding = PaddingValues(vertical = WHEEL_ITEM_HEIGHT * ((VISIBLE_ROWS - 1) / 2)),
        modifier = modifier.height(WHEEL_ITEM_HEIGHT * VISIBLE_ROWS)
    ) {
        items(items.size) { i ->
            val isSelected = i == state.firstVisibleItemIndex
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().height(WHEEL_ITEM_HEIGHT)
            ) {
                Text(
                    items[i],
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
