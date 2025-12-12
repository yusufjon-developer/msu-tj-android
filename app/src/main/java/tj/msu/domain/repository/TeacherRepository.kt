package tj.msu.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.msu.domain.model.TeacherModel

interface TeacherRepository {
    fun getTeachers(): Flow<List<TeacherModel>>
}
