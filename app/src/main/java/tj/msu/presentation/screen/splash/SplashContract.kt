package tj.msu.presentation.screen.splash

import tj.msu.presentation.core.base.UiEffect
import tj.msu.presentation.core.base.UiEvent
import tj.msu.presentation.core.base.UiState

data class SplashState(
    val isLoading: Boolean = true
) : UiState

sealed interface SplashEvent : UiEvent {
    data object StartLoading : SplashEvent
}

sealed interface SplashEffect : UiEffect {
    data object NavigateToMain : SplashEffect
    data object NavigateToAuth : SplashEffect
}
