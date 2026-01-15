package tj.msu.presentation.screen.schedule

import tj.msu.domain.model.Lesson
import tj.msu.presentation.core.base.UiEffect
import tj.msu.presentation.core.base.UiEvent
import tj.msu.presentation.core.base.UiState

data class ScheduleState(
    val isLoading: Boolean = false,
    val scheduleByDay: Map<Int, List<Lesson>> = emptyMap(),
    val error: String? = null,
    val selectedFacultyCode: String = "pmi",
    val selectedCourse: Int = 3,
    val faculties: Map<String, String> = mapOf(
        "pmi" to "ПМИ",
        "geo" to "Геология",
        "mo" to "МО",
        "ling" to "Лингвистика",
        "gmu" to "ГМУ",
        "hfmm" to "ХФММ"
    ),
    val courses: List<Int> = listOf(1, 2, 3, 4),
    val isNextWeek: Boolean = false,
    val weekDates: List<String> = emptyList(),
    val isNextWeekAvailable: Boolean = false,
    val isSmartFreeRooms: Boolean = false,
    val isExpandableFreeRooms: Boolean = false
) : UiState

sealed interface ScheduleEvent : UiEvent {
    data object LoadData : ScheduleEvent
    data class OnApplyFilters(val facultyCode: String, val course: Int) : ScheduleEvent
    data class OnLessonClick(val name: String) : ScheduleEvent
    data object OnToggleNextWeek : ScheduleEvent
}

sealed interface ScheduleEffect : UiEffect {
    data class ShowToast(val message: String) : ScheduleEffect
    data class NavigateToDetails(val name: String) : ScheduleEffect
}