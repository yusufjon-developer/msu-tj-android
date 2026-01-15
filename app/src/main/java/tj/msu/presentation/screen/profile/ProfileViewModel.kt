package tj.msu.presentation.screen.profile

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import tj.msu.data.repository.UserPreferencesRepository
import tj.msu.domain.repository.AuthRepository
import tj.msu.presentation.core.base.MVIViewModel

@KoinViewModel
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userPrefs: UserPreferencesRepository
) : MVIViewModel<ProfileEvent, ProfileEffect, ProfileState>() {

    override fun createInitialState(): ProfileState = ProfileState()

    init {
        setEvent(ProfileEvent.LoadProfile)
    }

    override fun handleEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.LoadProfile -> loadUserData()

            is ProfileEvent.OnLogout -> {
                authRepository.signOut()
            }

            is ProfileEvent.OnToggleLayout -> {
                viewModelScope.launch {
                    userPrefs.setFreeRoomsLayout(event.isExpandable)
                    setState { copy(isExpandableFreeRooms = event.isExpandable) }
                }
            }

            is ProfileEvent.OnToggleSmartFreeRooms -> {
                viewModelScope.launch {
                    userPrefs.setSmartFreeRooms(event.isEnabled)
                    setState { copy(isSmartFreeRooms = event.isEnabled) }
                }
            }

            is ProfileEvent.OnUpdateGroup -> updateGroup(event.facultyCode, event.course)
        }
    }

    private fun loadUserData() {
        val user = authRepository.currentUser ?: return

        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val profileResult = authRepository.getUserProfile(user.uid)
            val localSettings = userPrefs.userProfile.firstOrNull()

            profileResult.onSuccess { profile ->
                if (profile != null) {
                    setState {
                        copy(
                            isLoading = false,
                            name = profile.name,
                            email = profile.email,
                            facultyCode = profile.facultyCode,
                            course = profile.course,
                            isExpandableFreeRooms = localSettings?.isExpandableFreeRooms ?: true,
                            isSmartFreeRooms = localSettings?.isSmartFreeRooms ?: false
                        )
                    }
                } else {
                    setState { copy(isLoading = false) }
                }
            }.onFailure {
                setState { copy(isLoading = false) }
            }
        }
    }

    private fun updateGroup(faculty: String, course: Int) {
        val user = authRepository.currentUser ?: return
        val oldFaculty = currentState.facultyCode
        val oldCourse = currentState.course

        viewModelScope.launch {
            setState { copy(isLoading = true) }

            authRepository.unsubscribeFromGroupNotifications(oldFaculty, oldCourse)

            val result = authRepository.saveUserProfile(
                uid = user.uid,
                name = currentState.name,
                faculty = faculty,
                course = course
            )

            result.onSuccess {
                setState {
                    copy(
                        isLoading = false,
                        facultyCode = faculty,
                        course = course
                    )
                }
                setEffect { ProfileEffect.ShowToast("Данные обновлены") }
            }.onFailure { e ->
                setState { copy(isLoading = false) }
                setEffect { ProfileEffect.ShowToast("Ошибка: ${e.localizedMessage}") }
            }
        }
    }
}