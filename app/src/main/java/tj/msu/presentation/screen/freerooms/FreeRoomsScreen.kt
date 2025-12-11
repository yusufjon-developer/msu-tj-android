package tj.msu.presentation.screen.freerooms

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import tj.msu.presentation.screen.schedule.components.DaySelector
import tj.msu.presentation.theme.MsuBackground
import tj.msu.presentation.theme.MsuBlue

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun FreeRoomsScreen(
    viewModel: FreeRoomsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = state.currentDayIndex,
        pageCount = { 7 }
    )

    LaunchedEffect(pagerState.currentPage) {
        if (state.currentDayIndex != pagerState.currentPage) {
            viewModel.setEvent(FreeRoomsEvent.SelectDay(pagerState.currentPage))
        }
    }

    LaunchedEffect(state.currentDayIndex) {
        if (pagerState.currentPage != state.currentDayIndex) {
            pagerState.scrollToPage(state.currentDayIndex)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        DaySelector(
            selectedDayIndex = pagerState.currentPage,
            onDaySelected = { index ->
                scope.launch { pagerState.scrollToPage(index) }
            }
        )

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MsuBlue)
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { pageIndex ->

                val pairs = state.freeRoomsByDay[pageIndex] ?: emptyList()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = pairs,
                        key = { it.pairNumber }
                    ) { pair ->
                        FreeRoomCardItem(pair)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FreeRoomCardItem(pair: PairFreeRooms) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(MsuBlue)
            )

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(75.dp)
                    .background(MsuBackground)
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val times = pair.time.split("\n")
                Text(
                    text = times.getOrNull(0) ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = times.getOrNull(1) ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                if (pair.freeRooms.isNotEmpty()) {
                    Text(
                        text = "Свободные аудитории:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.freeRooms.forEach { room ->
                            RoomChip(room)
                        }
                    }
                } else {
                    Text(
                        text = "Все аудитории заняты",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun RoomChip(room: String) {
    Box(
        modifier = Modifier
            .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
            .background(Color(0xFFF1F8E9), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = room,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
    }
}