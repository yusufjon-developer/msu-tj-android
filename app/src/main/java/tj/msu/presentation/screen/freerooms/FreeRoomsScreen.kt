package tj.msu.presentation.screen.freerooms

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import tj.msu.presentation.components.DaySelector
import tj.msu.presentation.screen.freerooms.components.ClassicFreeRoomCardItem
import tj.msu.presentation.screen.freerooms.components.ExpandableFreeRoomCardItem
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
                        if (state.isExpandableLayout) {
                            ExpandableFreeRoomCardItem(pair)
                        } else {
                            ClassicFreeRoomCardItem(pair)
                        }
                    }
                }
            }
        }
    }
}