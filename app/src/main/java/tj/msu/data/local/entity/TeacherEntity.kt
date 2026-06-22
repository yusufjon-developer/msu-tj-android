package tj.msu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import tj.msu.domain.model.DayScheduleModel

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey val id: String, // Format: teacherId_isNextWeek
    val teacherId: String,
    val name: String,
    val isNextWeek: Boolean,
    val days: List<DayScheduleModel>
)
