package tj.msu.presentation.screen.main

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import tj.msu.data.repository.UserPreferencesRepository
import tj.msu.domain.repository.AuthRepository
import tj.msu.domain.repository.NotificationRepository
import tj.msu.presentation.core.base.MVIViewModel

@KoinViewModel
class MainViewModel(
    private val authRepository: AuthRepository,
    private val userPrefs: UserPreferencesRepository,
    private val notificationRepository: NotificationRepository
) : MVIViewModel<MainEvent, MainEffect, MainState>() {

    override fun createInitialState(): MainState = MainState()

    init {
        observeAuthState()
        observeNotifications()
        observeUserRole()
        viewModelScope.launch {
            authRepository.saveFcmToken()
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState.collectLatest { user ->
                if (user == null) {
                    setState {
                        copy(isLoading = false, isAuthorized = false, unreadNotificationsCount = 0)
                    }
                } else {
                    checkUserProfile(user.uid)
                }
            }
        }
    }

    private fun checkUserProfile(uid: String) {
        viewModelScope.launch {

            val result = authRepository.getUserProfile(uid)
            val profile = result.getOrNull()

            setState {
                copy(
                    isLoading = false,
                    isAuthorized = profile != null
                )
            }
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            authRepository.authState.collectLatest { user ->
                if (user != null) {
                    notificationRepository.getNotifications(user.uid)
                        .catch { e ->

                            e.printStackTrace()
                        }
                        .collect { list ->
                            val count = list.count { !it.isRead }
                            setState { copy(unreadNotificationsCount = count) }
                        }
                } else {
                    setState { copy(unreadNotificationsCount = 0) }
                }
            }
        }
    }

    private fun observeUserRole() {
        viewModelScope.launch {
            userPrefs.userProfile.collectLatest { profile ->
                setState { copy(userRole = profile?.role ?: "student") }
            }
        }
    }

    override fun handleEvent(event: MainEvent) {
        when (event) {
            is MainEvent.OnSignOut -> {
                viewModelScope.launch {
                    userPrefs.clear()
                    authRepository.signOut()
                }
            }
            is MainEvent.CheckAuth -> {
                val user = authRepository.currentUser
                if (user != null) {
                    setState { copy(isLoading = true) }
                    checkUserProfile(user.uid)
                } else {
                    setState { copy(isAuthorized = false, isLoading = false) }
                }
            }
        }
    }
}