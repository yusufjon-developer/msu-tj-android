package tj.msu.data.model

import com.google.firebase.database.PropertyName

data class GroupScheduleDto(
    var id: String? = null,
    var title: String? = null,
    @get:PropertyName("days") @set:PropertyName("days")
    var days: Any? = null
)

data class DayScheduleDto(
    var day: String? = null,
    @get:PropertyName("lessons") @set:PropertyName("lessons")
    var lessons: List<LessonDto?>? = null
)

data class LessonDto(
    var subject: String? = null,
    var type: String? = null,
    var teacher: List<String>? = null,
    var rooms: List<String>? = null
)