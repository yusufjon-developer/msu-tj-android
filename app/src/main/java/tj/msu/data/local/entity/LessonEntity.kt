package tj.msu.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lessons",
    indices = [Index(value = ["groupId", "dayIndex"])]
)
data class LessonEntity(
    @PrimaryKey val id: String, // Format: groupId_isNextWeek_dayIndex_pairNumber
    val groupId: String,
    val isNextWeek: Boolean,
    val dayIndex: Int,
    val title: String,
    val time: String,
    val type: String, // String representation of LessonType
    val teacher: String,
    val room: String,
    val date: String?
)
