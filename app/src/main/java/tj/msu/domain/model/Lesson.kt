package tj.msu.domain.model

data class Lesson(
    val id: String,
    val title: String,
    val time: String,
    val type: LessonType,
    val teacher: String,
    val room: String,
    val dayIndex: Int,
    val date: String? = null
)

enum class LessonType(val displayName: String) {
    LECTURE("Лекция"),
    PRACTICE("Практика"),
    SEMINAR("Семинар"),
    LAB("Лаб. работа"),
    EXAM("Экзамен"),
    CREDIT("Зачет"),
    CONSULTATION("Консультация"),
    WINDOW("Свободно"),
    UNKNOWN("Неизвестно")
}