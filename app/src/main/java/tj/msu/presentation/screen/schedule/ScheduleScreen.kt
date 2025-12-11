package tj.msu.presentation.screen.schedule

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import tj.msu.presentation.screen.schedule.components.DaySelector
import tj.msu.presentation.screen.schedule.components.GroupSelectionCard
import tj.msu.presentation.screen.schedule.components.LessonItem
import tj.msu.presentation.screen.schedule.components.SelectionBottomSheet
import tj.msu.presentation.theme.MsuBlue
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentDayOfWeek = LocalDate.now().dayOfWeek.value - 1
    val initialPage = if (currentDayOfWeek in 0..6) currentDayOfWeek else 0
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { 7 }
    )

    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ScheduleEffect.ShowToast -> Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                is ScheduleEffect.NavigateToDetails -> {}
            }
        }
    }

    if (showBottomSheet) {
        SelectionBottomSheet(
            faculties = state.faculties,
            courses = state.courses,
            initialFacultyCode = state.selectedFacultyCode,
            initialCourse = state.selectedCourse,
            onApply = { faculty, course ->
                viewModel.setEvent(ScheduleEvent.OnApplyFilters(faculty, course))
                showBottomSheet = false
            },
            onDismiss = { showBottomSheet = false }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        GroupSelectionCard(
            facultyName = state.faculties[state.selectedFacultyCode] ?: "Факультет",
            course = state.selectedCourse,
            onClick = { showBottomSheet = true }
        )

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

                val lessonsForDay = state.scheduleByDay[pageIndex] ?: emptyList()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = lessonsForDay,
                        key = { lesson -> lesson.id }
                    ) { lesson ->
                        LessonItem(
                            lesson = lesson,
                            onClick = {
                                if (lesson.type != tj.msu.domain.model.LessonType.WINDOW) {
                                    viewModel.setEvent(ScheduleEvent.OnLessonClick(lesson.title))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}