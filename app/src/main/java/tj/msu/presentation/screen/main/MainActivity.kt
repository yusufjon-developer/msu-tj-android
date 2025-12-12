package tj.msu.presentation.screen.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import org.koin.androidx.compose.koinViewModel
import tj.msu.presentation.screen.auth.AuthScreen
import tj.msu.presentation.theme.MsuBlue
import tj.msu.presentation.theme.MsuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        subscribeToTopics()

        setContent {
            MsuTheme {
                RequestNotificationPermission()

                val mainViewModel: MainViewModel = koinViewModel()
                val state by mainViewModel.uiState.collectAsStateWithLifecycle()

                when {
                    state.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MsuBlue)
                        }
                    }

                    state.isAuthorized -> {
                        MainScreen(unreadNotificationsCount = state.unreadNotificationsCount)
                    }

                    else -> {
                        AuthScreen(
                            onAuthSuccess = { mainViewModel.setEvent(MainEvent.CheckAuth) }
                        )
                    }
                }
            }
        }
    }

    private fun subscribeToTopics() {
        Firebase.messaging.subscribeToTopic("announcements")
        Firebase.messaging.subscribeToTopic("updates")
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                   
                }
            }
    }

    @Composable
    private fun RequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val context = androidx.compose.ui.platform.LocalContext.current
            var hasPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                )
            }

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted -> hasPermission = isGranted }
            )

            LaunchedEffect(Unit) {
                if (!hasPermission) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}