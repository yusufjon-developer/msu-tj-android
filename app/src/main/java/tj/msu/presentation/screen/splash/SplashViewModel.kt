package tj.msu.presentation.screen.splash

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.android.annotation.KoinViewModel
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
        }
    }

    private fun startLoadingProcess() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            var isAuthSuccess = false
            
            withTimeoutOrNull(3000L) {
                 val currentUser = authRepository.currentUser
                 if (currentUser != null) {
                     val profileResult = authRepository.getUserProfile(currentUser.uid)
                     val profile = profileResult.getOrNull()
                     
                     if (profile != null) {
                         val groupId = "${profile.facultyCode}_${profile.course}"
                         try {

                         } catch (e: Exception) {
                             e.printStackTrace()
                         }
                         isAuthSuccess = true
                     } else {
                         isAuthSuccess = true
                     }
                 }
            }

            val elapsedTime = System.currentTimeMillis() - startTime
            val minWait = 1200L
            
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
}
