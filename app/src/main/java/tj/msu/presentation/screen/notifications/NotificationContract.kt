package tj.msu.presentation.screen.notifications

import tj.msu.domain.model.NotificationModel
import tj.msu.presentation.core.base.UiEffect
import tj.msu.presentation.core.base.UiEvent
import tj.msu.presentation.core.base.UiState

data class NotificationState(
    val isLoading: Boolean = false,
    val notifications: List<NotificationModel> = emptyList(),
    val error: String? = null
) : UiState

sealed interface NotificationEvent : UiEvent {
    data object LoadData : NotificationEvent
    data class OnNotificationClick(val id: String) : NotificationEvent
}

sealed interface NotificationEffect : UiEffect