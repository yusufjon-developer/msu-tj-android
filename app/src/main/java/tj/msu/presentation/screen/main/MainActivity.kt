package tj.msu.presentation.screen.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import tj.msu.presentation.screen.auth.AuthScreen
import tj.msu.presentation.theme.MsuBlue
import tj.msu.presentation.theme.MsuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MsuTheme {
                val mainViewModel: MainViewModel = koinViewModel()
                val state by mainViewModel.uiState.collectAsStateWithLifecycle()

                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MsuBlue)
                        }
                    }

                    state.isAuthorized -> {
                        MainScreen(unreadNotificationsCount = state.unreadNotificationsCount)
                    }

                    else -> {
                        AuthScreen(
                            onAuthSuccess = {
                                mainViewModel.setEvent(MainEvent.CheckAuth)
                            }
                        )
                    }
                }
            }
        }
    }
}