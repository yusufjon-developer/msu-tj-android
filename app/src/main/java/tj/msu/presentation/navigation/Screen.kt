package tj.msu.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import tj.msu.R

sealed interface IconSource {
    data class Vector(val imageVector: ImageVector) : IconSource
    data class Resource(val id: Int) : IconSource
}

sealed class Screen(val route: String, val title: String, val icon: IconSource) {

    data object Schedule : Screen(
        "schedule",
        "Расписание",
        IconSource.Vector(Icons.Default.DateRange)
    )

    data object FreeRooms : Screen(
        "free_rooms",
        "Аудитории",
        IconSource.Vector(Icons.Default.Search)
    )

    data object Teachers : Screen(
        "teachers",
        "Преподаватели",
        IconSource.Resource(R.drawable.ic_teacher)
    )

    data object Profile : Screen(
        "profile",
        "Профиль",
        IconSource.Vector(Icons.Default.Person)
    )

    data object Notifications : Screen(
        "notifications",
        "Уведомления",
        IconSource.Vector(Icons.Default.Notifications)
    )

    data object Splash : Screen("splash", "Splash", IconSource.Resource(0))
    data object Auth : Screen("auth", "Auth", IconSource.Resource(0))
    data object Main : Screen("main", "Main", IconSource.Resource(0))
}