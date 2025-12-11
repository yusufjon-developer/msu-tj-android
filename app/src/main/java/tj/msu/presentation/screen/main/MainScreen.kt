package tj.msu.presentation.screen.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.compose.koinViewModel
import tj.msu.presentation.navigation.Screen
import tj.msu.presentation.screen.freerooms.FreeRoomsScreen
import tj.msu.presentation.screen.notifications.NotificationScreen
import tj.msu.presentation.screen.profile.ProfileScreen
import tj.msu.presentation.screen.schedule.ScheduleScreen
import tj.msu.presentation.theme.MsuBackground
import tj.msu.presentation.theme.MsuBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()

    val bottomNavScreens = listOf(
        Screen.Schedule,
        Screen.FreeRooms,
        Screen.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isMainScreen = bottomNavScreens.any { it.route == currentDestination?.route }

    val titleText = when (currentDestination?.route) {
        Screen.Notifications.route -> Screen.Notifications.title
        else -> bottomNavScreens.find { it.route == currentDestination?.route }?.title ?: "MSU TJ"
    }

    Scaffold(
        containerColor = MsuBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = titleText,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    if (!isMainScreen) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад",
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = {
                    if (isMainScreen) {
                        IconButton(onClick = {
                            navController.navigate(Screen.Notifications.route)
                        }) {
                            if (unreadCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge { Text(text = unreadCount.toString()) }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Уведомления",
                                        tint = Color.White
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Уведомления",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MsuBlue,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            if (isMainScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MsuBlue
                ) {
                    bottomNavScreens.forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title) },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MsuBlue,
                                selectedTextColor = MsuBlue,
                                indicatorColor = MsuBlue.copy(alpha = 0.1f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Schedule.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Schedule.route) { ScheduleScreen() }
            composable(Screen.FreeRooms.route) { FreeRoomsScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }

            composable(Screen.Notifications.route) {
                NotificationScreen()
            }
        }
    }
}