package tj.msu.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.msu.domain.model.FreeRooms
import tj.msu.domain.model.Lesson

interface ScheduleRepository {
    fun getDailySchedule(groupId: String): Flow<List<Lesson>>
    fun getFreeRooms(): Flow<FreeRooms>
}