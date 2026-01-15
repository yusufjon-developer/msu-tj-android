package tj.msu.presentation.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {
    fun getWeekDates(isNextWeek: Boolean): List<String> {
        val today = LocalDate.now()
        val currentMonday = today.with(DayOfWeek.MONDAY)
        val targetMonday = if (isNextWeek) currentMonday.plusWeeks(1) else currentMonday
        
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        
        return (0..6).map { dayIndex ->
            targetMonday.plusDays(dayIndex.toLong()).format(formatter)
        }
    }
}
