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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import org.koin.androidx.compose.koinViewModel
import tj.msu.presentation.navigation.Screen
import tj.msu.presentation.screen.auth.AuthScreen
import tj.msu.presentation.screen.splash.SplashScreen
import tj.msu.presentation.theme.MsuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        subscribeToTopics()

        setContent {
            MsuTheme {
                RequestNotificationPermission()

                val navController = rememberNavController()
                val mainViewModel: MainViewModel = koinViewModel()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Screen.Splash.route) {
                        SplashScreen(
                            onNavigateToMain = {
                                navController.navigate(Screen.Main.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            },
                            onNavigateToAuth = {
                                navController.navigate(Screen.Auth.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.Auth.route) {
                        AuthScreen(
                            onAuthSuccess = {
                                mainViewModel.setEvent(MainEvent.CheckAuth)
                                navController.navigate(Screen.Main.route) {
                                    popUpTo(Screen.Auth.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.Main.route) {
                        val state by mainViewModel.uiState.collectAsStateWithLifecycle()

                        LaunchedEffect(state.isAuthorized, state.isLoading) {
                            if (!state.isAuthorized && !state.isLoading) {
                                navController.navigate(Screen.Auth.route) {
                                    popUpTo(Screen.Main.route) { inclusive = true }
                                }
                            }
                        }

                        MainScreen(
                            unreadNotificationsCount = state.unreadNotificationsCount,
                            userRole = state.userRole
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