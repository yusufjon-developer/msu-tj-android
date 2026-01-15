package tj.msu.data.mapper

import tj.msu.data.model.TeacherDto
import tj.msu.domain.model.DayScheduleModel
import tj.msu.domain.model.Lesson
import tj.msu.domain.model.TeacherModel


fun TeacherDto.toDomain(originalId: String): TeacherModel {
    val displayName = this.name?.replace("_", ".") ?: originalId.replace("_", ".")


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


    val domainDays = daysList.mapIndexed { dayIndex, dayMap ->
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


        val dailyLessons = mutableListOf<Lesson>()
        for (pairIndex in 0 until 5) {
            val lessonRaw = lessonsMap[pairIndex]
            val pairNumber = pairIndex + 1

            if (lessonRaw != null) {
                val subject = lessonRaw["subject"] as? String
                val typeRaw = lessonRaw["type"] as? String


                val groups = parseListOrString(lessonRaw["teacher"])
                val room = parseListOrString(lessonRaw["rooms"])

                dailyLessons.add(
                    createLesson(
                        dayName = cleanDayName,
                        pairNumber = pairNumber,
                        dayIndex = dayIndex,
                        subject = subject,
                        typeRaw = typeRaw,
                        teacher = groups,
                        room = room,
                        date = date
                    )
                )
            } else {
                dailyLessons.add(createWindowLesson(cleanDayName, pairNumber, dayIndex))
            }
        }


        DayScheduleModel(
            dayIndex = dayIndex,
            dayName = cleanDayName,
            lessons = dailyLessons,
            date = date
        )
    }

    return TeacherModel(
        id = originalId,
        name = displayName,
        days = domainDays
    )
}

private fun parseListOrString(data: Any?): String {
    return when (data) {
        is List<*> -> data.joinToString(", ")
        is String -> data
        else -> ""
    }
}