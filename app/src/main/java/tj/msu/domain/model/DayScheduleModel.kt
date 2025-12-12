package tj.msu.domain.model

data class DayScheduleModel(
    val dayIndex: Int,
    val dayName: String,
    val lessons: List<Lesson>
)