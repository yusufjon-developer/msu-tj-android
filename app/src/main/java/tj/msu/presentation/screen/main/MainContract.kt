package tj.msu.presentation.screen.main

import tj.msu.presentation.core.base.UiEffect
import tj.msu.presentation.core.base.UiEvent
import tj.msu.presentation.core.base.UiState

data class MainState(
    val isLoading: Boolean = true,
    val isAuthorized: Boolean = false,
    val unreadNotificationsCount: Int = 0
) : UiState

sealed interface MainEvent : UiEvent {
    data object OnSignOut : MainEvent
    data object CheckAuth : MainEvent
}

sealed interface MainEffect : UiEffect