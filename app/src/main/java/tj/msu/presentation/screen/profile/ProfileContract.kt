package tj.msu.presentation.screen.profile

import tj.msu.presentation.core.base.UiEffect
import tj.msu.presentation.core.base.UiEvent
import tj.msu.presentation.core.base.UiState

data class ProfileState(
    val isLoading: Boolean = false,

    val name: String = "",
    val email: String = "",
    val facultyCode: String = "",
    val course: Int = 0,
    val isExpandableFreeRooms: Boolean = true,

    val faculties: Map<String, String> = mapOf(
        "pmi" to "ПМИ",
        "geo" to "Геология",
        "mo" to "МО",
        "ling" to "Лингвистика",
        "gmu" to "ГМУ",
        "hfmm" to "ХФММ"
    ),
    val courses: List<Int> = listOf(1, 2, 3, 4)
) : UiState

sealed interface ProfileEvent : UiEvent {
    data object LoadProfile : ProfileEvent
    data object OnLogout : ProfileEvent
    data class OnToggleLayout(val isExpandable: Boolean) : ProfileEvent
    data class OnUpdateGroup(val facultyCode: String, val course: Int) : ProfileEvent
}

sealed interface ProfileEffect : UiEffect {
    data class ShowToast(val message: String) : ProfileEffect
}