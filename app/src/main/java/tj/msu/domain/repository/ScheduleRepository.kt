package tj.msu.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.msu.domain.model.FreeRooms
import tj.msu.domain.model.Lesson

interface ScheduleRepository {
    fun getDailySchedule(groupId: String, isNextWeek: Boolean = false): Flow<List<Lesson>>
    fun getFreeRooms(isNextWeek: Boolean = false): Flow<FreeRooms>
    fun checkNextWeekScheduleAvailability(groupId: String): Flow<Boolean>
    fun checkNextWeekFreeRoomsAvailability(): Flow<Boolean>
}