package tj.msu.presentation.screen.auth

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import tj.msu.domain.repository.AuthRepository
import tj.msu.presentation.core.base.MVIViewModel

@KoinViewModel
class AuthViewModel(
    private val repository: AuthRepository
) : MVIViewModel<AuthEvent, AuthEffect, AuthState>() {

    override fun createInitialState(): AuthState = AuthState()

    fun resetState() {
        setState { AuthState() }
    }

    override fun handleEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.OnEmailChange -> setState { copy(email = event.email, error = null) }
            is AuthEvent.OnPassChange -> setState { copy(pass = event.pass, error = null) }
            is AuthEvent.OnNameChange -> setState { copy(name = event.name, error = null) }
            is AuthEvent.OnFacultyChange -> setState { copy(selectedFacultyCode = event.code) }
            is AuthEvent.OnCourseChange -> setState { copy(selectedCourse = event.course) }
            is AuthEvent.OnToggleMode -> setState { copy(isLoginMode = !isLoginMode, error = null, currentStep = AuthStep.CREDENTIALS) }
            is AuthEvent.OnNextStep -> handleNextStep()
            is AuthEvent.OnBackStep -> setState { copy(currentStep = AuthStep.CREDENTIALS) }

            is AuthEvent.OnSubmit -> handleNextStep()
            is AuthEvent.OnGoogleSignIn -> authWithGoogle(event.idToken)
        }
    }

    private fun handleNextStep() {
        val state = currentState

        if (state.currentStep == AuthStep.CREDENTIALS) {
            if (state.email.isBlank() || state.pass.isBlank()) {
                setState { copy(error = "Заполните Email и пароль") }
                return
            }
            if (state.isLoginMode) {
                performLogin()
            } else {
                setState { copy(currentStep = AuthStep.PROFILE_INFO, error = null) }
            }
        } else {
            if (state.name.isBlank()) {
                setState { copy(error = "Введите ваше имя") }
                return
            }
            performFullRegistration()
        }
    }

    private fun performLogin() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            repository.signInWithEmail(currentState.email, currentState.pass)
                .onSuccess { setEffect { AuthEffect.NavigateToMain } }
                .onFailure { setState { copy(isLoading = false, error = it.localizedMessage) } }
        }
    }

    private fun performFullRegistration() {
        val state = currentState
        viewModelScope.launch {
            setState { copy(isLoading = true) }

            val currentUser = repository.currentUser
            val result = if (currentUser != null) {
                repository.saveUserProfile(
                    uid = currentUser.uid,
                    name = state.name,
                    faculty = state.selectedFacultyCode,
                    course = state.selectedCourse
                )
            } else {
                repository.signUpWithEmail(
                    email = state.email,
                    pass = state.pass,
                    name = state.name,
                    faculty = state.selectedFacultyCode,
                    course = state.selectedCourse
                )
            }

            result.onSuccess {
                setEffect { AuthEffect.NavigateToMain }
            }.onFailure {
                setState { copy(isLoading = false, error = it.localizedMessage) }
            }
        }
    }

    private fun authWithGoogle(token: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true) }

            val signInResult = repository.signInWithGoogle(token)

            signInResult.onFailure {
                setState { copy(isLoading = false, error = it.localizedMessage) }
                return@launch
            }

            val uid = repository.currentUser?.uid ?: ""
            val profileResult = repository.getUserProfile(uid)
            val profile = profileResult.getOrNull()

            if (profile != null) {
                setState { copy(isLoading = false) }
                setEffect { AuthEffect.NavigateToMain }
            } else {
                val googleName = repository.currentUser?.displayName ?: ""

                setState {
                    copy(
                        isLoading = false,
                        currentStep = AuthStep.PROFILE_INFO,
                        name = googleName,
                        isLoginMode = false
                    )
                }
            }
        }
    }
}