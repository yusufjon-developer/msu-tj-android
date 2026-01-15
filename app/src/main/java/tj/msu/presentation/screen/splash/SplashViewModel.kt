package tj.msu.presentation.screen.splash

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.android.annotation.KoinViewModel
import tj.msu.BuildConfig
import tj.msu.domain.repository.AuthRepository
import tj.msu.presentation.core.base.MVIViewModel

@KoinViewModel
class SplashViewModel(
    private val authRepository: AuthRepository
) : MVIViewModel<SplashEvent, SplashEffect, SplashState>() {

    override fun createInitialState(): SplashState = SplashState()

    init {
        setEvent(SplashEvent.StartLoading)
    }

    override fun handleEvent(event: SplashEvent) {
        when (event) {
            is SplashEvent.StartLoading -> startLoadingProcess()
            is SplashEvent.OnSkipUpdate -> {
                setState { copy(updateInfo = null) }
                checkAuthAndNavigate()
            }
        }
    }

    private fun startLoadingProcess() {
        viewModelScope.launch {
            val appInfoResult = authRepository.getAppInfo()
            val appInfo = appInfoResult.getOrNull()

            if (appInfo != null && isUpdateNeeded(BuildConfig.VERSION_NAME, appInfo.latestVersion)) {
                setState { copy(updateInfo = appInfo) }
            } else {
                checkAuthAndNavigate()
            }
        }
    }

    private fun checkAuthAndNavigate() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            var isAuthSuccess = false
            
            withTimeoutOrNull(3000L) {
                 val currentUser = authRepository.currentUser
                 if (currentUser != null) {
                     val profileResult = authRepository.getUserProfile(currentUser.uid)
                     val profile = profileResult.getOrNull()
                     
                     if (profile != null) {

                         isAuthSuccess = true
                     } else {
                         isAuthSuccess = true
                     }
                 }
            }

            val elapsedTime = System.currentTimeMillis() - startTime
            val minWait = 1000L
            
            if (elapsedTime < minWait) {
                delay(minWait - elapsedTime)
            }

            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                 setEffect { SplashEffect.NavigateToMain }
            } else {
                 setEffect { SplashEffect.NavigateToAuth }
            }
        }
    }

    private fun isUpdateNeeded(currentVersion: String, latestVersion: String): Boolean {
        if (currentVersion.isBlank() || latestVersion.isBlank()) return false
        
        val currentParts = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val latestParts = latestVersion.split(".").map { it.toIntOrNull() ?: 0 }
        
        val length = maxOf(currentParts.size, latestParts.size)
        
        for (i in 0 until length) {
            val currentPart = currentParts.getOrElse(i) { 0 }
            val latestPart = latestParts.getOrElse(i) { 0 }
            
            if (latestPart > currentPart) return true
            if (latestPart < currentPart) return false
        }
        
        return false
    }
}
