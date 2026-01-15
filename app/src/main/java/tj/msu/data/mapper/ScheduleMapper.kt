package tj.msu.data.mapper

import tj.msu.data.model.GroupScheduleDto
import tj.msu.domain.model.Lesson
import tj.msu.domain.model.LessonType

fun GroupScheduleDto.toDomainList(): List<Lesson> {
    val result = mutableListOf<Lesson>()

    val daysList: List<Map<String, Any?>> = when (val d = this.days) {
        is List<*> -> {
            d.filterIsInstance<Map<String, Any?>>()
        }
        is Map<*, *> -> {
            d.entries
                .sortedBy { (it.key as? String)?.toIntOrNull() ?: 0 }
                .mapNotNull { it.value as? Map<String, Any?> }
        }
        else -> emptyList()
    }

    daysList.forEachIndexed { dayIndex, dayMap ->
        val rawDayName = dayMap["day"] as? String
        val date = dayMap["date"] as? String
        val cleanDayName = rawDayName?.trim() ?: getDayNameByIndex(dayIndex)

        val lessonsData = dayMap["lessons"]

        val lessonsMap = mutableMapOf<Int, Map<String, Any?>>()

        when (lessonsData) {
            is List<*> -> {
                lessonsData.forEachIndexed { index, lessonObj ->
                    if (lessonObj is Map<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        lessonsMap[index] = lessonObj as Map<String, Any?>
                    }
                }
            }
            is Map<*, *> -> {
                lessonsData.forEach { (key, value) ->
                    val pairIdx = key.toString().toIntOrNull()
                    if (pairIdx != null && value is Map<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        lessonsMap[pairIdx] = value as Map<String, Any?>
                    }
                }
            }
        }

        for (pairIndex in 0 until 5) {
            val lessonRaw = lessonsMap[pairIndex]
            val pairNumber = pairIndex + 1

            if (lessonRaw != null) {
                val subject = lessonRaw["subject"] as? String
                val typeRaw = lessonRaw["type"] as? String

                val teacher = parseListOrString(lessonRaw["teacher"])
                val room = parseListOrString(lessonRaw["rooms"])

                result.add(createLesson(
                    dayName = cleanDayName,
                    pairNumber = pairNumber,
                    dayIndex = dayIndex,
                    subject = subject,
                    typeRaw = typeRaw,
                    teacher = teacher,
                    room = room,
                    date = date
                ))
            } else {
                result.add(createWindowLesson(cleanDayName, pairNumber, dayIndex, date))
            }
        }
    }

    return result.sortedBy { it.time }
}

private fun parseListOrString(data: Any?): String {
    return when (data) {
        is List<*> -> data.joinToString(", ")
        is String -> data
        else -> ""
    }
}

fun getDayNameByIndex(index: Int): String {
    return when(index) {
        0 -> "Понедельник"
        1 -> "Вторник"
        2 -> "Среда"
        3 -> "Четверг"
        4 -> "Пятница"
        5 -> "Суббота"
        6 -> "Воскресенье"
        else -> "День $index"
    }
}

fun createLesson(
    dayName: String,
    pairNumber: Int,
    dayIndex: Int,
    subject: String?,
    typeRaw: String?,
    teacher: String,
    room: String,
    date: String? = null
): Lesson {
    val rawType = typeRaw?.lowercase() ?: ""

    return Lesson(
        id = "${dayName}_${pairNumber}",
        title = subject ?: "Нет предмета",
        time = calculateTime(pairNumber),
        teacher = teacher,
        room = room,
        dayIndex = dayIndex,
        date = date,
        type = when {
            rawType.contains("лекция") -> LessonType.LECTURE
            rawType.contains("практика") -> LessonType.PRACTICE
            rawType.contains("семинар") -> LessonType.SEMINAR
            rawType.contains("лаб") -> LessonType.LAB
            rawType.contains("экзамен") -> LessonType.EXAM
            rawType.contains("зачет") -> LessonType.CREDIT
            rawType.contains("консультация") -> LessonType.CONSULTATION
            else -> LessonType.UNKNOWN
        }
    )
}

fun createWindowLesson(dayName: String, pairNumber: Int, dayIndex: Int, date: String? = null): Lesson {
    return Lesson(
        id = "window_${dayName}_${pairNumber}",
        title = "Свободная пара",
        time = calculateTime(pairNumber),
        teacher = "",
        room = "",
        dayIndex = dayIndex,
        type = LessonType.WINDOW,
        date = date
    )
}

private fun calculateTime(pairNum: Int): String {
    val startHour = 8
    val startMinute = 0
    val lessonDuration = 90
    val smallBreak = 15
    val bigBreak = 60

    var currentMinutes = startHour * 60 + startMinute

    for (i in 1 until pairNum) {
        currentMinutes += lessonDuration
        if (i == 3) {
            currentMinutes += bigBreak
        } else {
            currentMinutes += smallBreak
        }
    }

    val startH = currentMinutes / 60
    val startM = currentMinutes % 60
    val endTotal = currentMinutes + lessonDuration
    val endH = endTotal / 60
    val endM = endTotal % 60

    return String.format("%02d:%02d\n%02d:%02d", startH, startM, endH, endM)
}