package tj.msu.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Schedule : Screen("schedule", "Расписание", Icons.Default.DateRange)
    data object FreeRooms : Screen("free_rooms", "Аудитории", Icons.Default.Search)
    data object Profile : Screen("profile", "Профиль", Icons.Default.Person)
    data object Notifications : Screen("notifications", "Уведомления", Icons.Default.Notifications)
}