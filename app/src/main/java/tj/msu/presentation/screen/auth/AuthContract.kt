package tj.msu.presentation.screen.auth

import tj.msu.presentation.core.base.UiEffect
import tj.msu.presentation.core.base.UiEvent
import tj.msu.presentation.core.base.UiState

enum class AuthStep {
    CREDENTIALS,
    PROFILE_INFO
}

data class AuthState(
    val isLoading: Boolean = false,
    val currentStep: AuthStep = AuthStep.CREDENTIALS,

    val email: String = "",
    val pass: String = "",
    
    val surname: String = "",
    val firstName: String = "",
    val patronymic: String = "",
    val hasNoPatronymic: Boolean = false,
    val selectedRole: String = "student",

    val selectedFacultyCode: String = "pmi",
    val selectedCourse: Int = 1,

    val isLoginMode: Boolean = true,
    val error: String? = null,

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

sealed interface AuthEvent : UiEvent {
    data class OnEmailChange(val email: String) : AuthEvent
    data class OnPassChange(val pass: String) : AuthEvent
    
    data class OnSurnameChange(val value: String) : AuthEvent
    data class OnFirstNameChange(val value: String) : AuthEvent
    data class OnPatronymicChange(val value: String) : AuthEvent
    data class OnTogglePatronymicVisibility(val isHidden: Boolean) : AuthEvent
    data class OnRoleChange(val role: String) : AuthEvent
    data class OnFacultyChange(val code: String) : AuthEvent
    data class OnCourseChange(val course: Int) : AuthEvent

    data object OnToggleMode : AuthEvent
    data object OnNextStep : AuthEvent
    data object OnBackStep : AuthEvent
    data object OnSubmit : AuthEvent
    data class OnGoogleSignIn(val idToken: String) : AuthEvent
}

sealed interface AuthEffect : UiEffect {
    data object NavigateToMain : AuthEffect
    data class ShowError(val message: String) : AuthEffect
}