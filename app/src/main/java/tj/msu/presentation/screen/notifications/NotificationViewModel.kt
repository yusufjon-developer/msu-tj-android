package tj.msu.presentation.screen.notifications

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import tj.msu.domain.repository.AuthRepository
import tj.msu.domain.repository.NotificationRepository
import tj.msu.presentation.core.base.MVIViewModel

@KoinViewModel
class NotificationViewModel(
    private val repository: NotificationRepository,
    private val authRepository: AuthRepository
) : MVIViewModel<NotificationEvent, NotificationEffect, NotificationState>() {

    override fun createInitialState(): NotificationState = NotificationState()

    init {
        setEvent(NotificationEvent.LoadData)
    }

    override fun handleEvent(event: NotificationEvent) {
        when (event) {
            is NotificationEvent.LoadData -> loadNotifications()
            is NotificationEvent.OnNotificationClick -> markAsRead(event.id)
        }
    }

    private fun loadNotifications() {
        val user = authRepository.currentUser ?: return

        viewModelScope.launch {
            repository.getNotifications(user.uid)
                .catch { e -> setState { copy(isLoading = false, error = e.message) } }
                .collect { list ->
                    setState { copy(isLoading = false, notifications = list) }
                }
        }
    }

    private fun markAsRead(id: String) {
        val user = authRepository.currentUser ?: return
        viewModelScope.launch {
            repository.markAsRead(user.uid, id)
        }
    }
}