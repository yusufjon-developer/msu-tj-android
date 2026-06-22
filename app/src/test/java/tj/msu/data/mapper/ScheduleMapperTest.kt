package tj.msu.data.mapper

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import tj.msu.data.local.entity.LessonEntity
import tj.msu.domain.model.Lesson
import tj.msu.domain.model.LessonType

class ScheduleMapperTest {

    @Test
    fun `map Lesson to LessonEntity correctly`() {
        val lesson = Lesson(
            id = "Mon_1",
            title = "Физика",
            time = "08:00\n09:30",
            type = LessonType.LECTURE,
            teacher = "Иванов И.И.",
            room = "202",
            dayIndex = 0,
            date = "2026-06-22"
        )

        val entity = lesson.toEntity(groupId = "pmi_3", isNextWeek = false)

        assertEquals("pmi_3_false_0_Mon_1", entity.id)
        assertEquals("pmi_3", entity.groupId)
        assertFalse(entity.isNextWeek)
        assertEquals(0, entity.dayIndex)
        assertEquals("Физика", entity.title)
        assertEquals("08:00\n09:30", entity.time)
        assertEquals("LECTURE", entity.type)
        assertEquals("Иванов И.И.", entity.teacher)
        assertEquals("202", entity.room)
        assertEquals("2026-06-22", entity.date)
    }

    @Test
    fun `map LessonEntity to Lesson correctly`() {
        val entity = LessonEntity(
            id = "pmi_3_false_0_Mon_1",
            groupId = "pmi_3",
            isNextWeek = false,
            dayIndex = 0,
            title = "Физика",
            time = "08:00\n09:30",
            type = "LECTURE",
            teacher = "Иванов И.И.",
            room = "202",
            date = "2026-06-22"
        )

        val lesson = entity.toDomain()

        assertEquals("pmi_3_false_0_Mon_1", lesson.id)
        assertEquals("Физика", lesson.title)
        assertEquals("08:00\n09:30", lesson.time)
        assertEquals(LessonType.LECTURE, lesson.type)
        assertEquals("Иванов И.И.", lesson.teacher)
        assertEquals("202", lesson.room)
        assertEquals(0, lesson.dayIndex)
        assertEquals("2026-06-22", lesson.date)
    }
}
