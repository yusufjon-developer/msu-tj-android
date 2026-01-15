package tj.msu.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.msu.domain.model.TeacherModel

interface TeacherRepository {
    fun getTeachers(isNextWeek: Boolean = false): Flow<List<TeacherModel>>
    fun checkNextWeekTeachersAvailability(): Flow<Boolean>
}
