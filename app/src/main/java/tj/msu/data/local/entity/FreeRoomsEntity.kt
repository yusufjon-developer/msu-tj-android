package tj.msu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "free_rooms")
data class FreeRoomsEntity(
    @PrimaryKey val isNextWeek: Boolean,
    val schedule: Map<String, Map<String, List<String>>>,
    val lastUpdate: String
)
