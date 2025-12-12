package tj.msu.domain.model

data class TeacherModel(
    val id: String,
    val name: String,
    val days: List<DayScheduleModel> = emptyList()
)