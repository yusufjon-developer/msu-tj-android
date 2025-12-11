package tj.msu.presentation.screen.freerooms.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tj.msu.presentation.theme.MsuBlue

@Composable
fun DayFilter(
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val days = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(days) { index, day ->
            FilterChip(
                selected = selectedIndex == index,
                onClick = { onSelect(index) },
                label = { Text(day) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MsuBlue,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun PairFilter(
    selectedPair: Int,
    onSelect: (Int) -> Unit
) {
    val pairs = listOf(1, 2, 3, 4, 5)

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(pairs) { pair ->
            FilterChip(
                selected = selectedPair == pair,
                onClick = { onSelect(pair) },
                label = { Text("$pair пара") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MsuBlue,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}