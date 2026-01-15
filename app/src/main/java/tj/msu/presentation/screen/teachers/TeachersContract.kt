
package tj.msu.presentation.screen.teachers

import tj.msu.domain.model.TeacherModel
import tj.msu.presentation.core.base.UiEffect
import tj.msu.presentation.core.base.UiEvent
import tj.msu.presentation.core.base.UiState

data class TeachersState(
    val isLoading: Boolean = false,
    val allTeachers: List<TeacherModel> = emptyList(),
    val filteredTeachers: List<TeacherModel> = emptyList(),
    val selectedTeacher: TeacherModel? = null,
    val searchQuery: String = "",
    val isNextWeek: Boolean = false,
    val weekDates: List<String> = emptyList(),
    val isNextWeekAvailable: Boolean = false
) : UiState

sealed class TeachersEvent : UiEvent {
    data object LoadData : TeachersEvent()
    data class OnSearch(val query: String) : TeachersEvent()
    data class OnSelectTeacher(val teacher: TeacherModel) : TeachersEvent()
    data object OnResetSearch : TeachersEvent()
    data object OnToggleNextWeek : TeachersEvent()
}

sealed class TeachersEffect : UiEffect {
    data class ShowError(val message: String) : TeachersEffect()
}