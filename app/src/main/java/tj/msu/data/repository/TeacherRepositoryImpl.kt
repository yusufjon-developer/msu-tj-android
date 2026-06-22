package tj.msu.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import tj.msu.data.local.dao.TeacherDao
import tj.msu.data.mapper.toDomain
import tj.msu.data.mapper.toEntity
import tj.msu.data.model.TeacherDto
import tj.msu.domain.model.TeacherModel
import tj.msu.domain.repository.TeacherRepository

@Single
class TeacherRepositoryImpl(
    private val db: FirebaseDatabase,
    private val teacherDao: TeacherDao
) : TeacherRepository {

    override fun getTeachers(isNextWeek: Boolean): Flow<List<TeacherModel>> = callbackFlow {
        val roomJob = launch(Dispatchers.IO) {
            teacherDao.getTeachers(isNextWeek).collect { entities ->
                trySend(entities.map { it.toDomain() })
            }
        }

        val path = if (isNextWeek) "teachers_next" else "teachers"
        val ref = db.getReference(path)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                launch(Dispatchers.IO) {
                    val teachers = snapshot.children.mapNotNull { child ->
                        val dto = child.getValue(TeacherDto::class.java)
                        val originalId = child.key ?: return@mapNotNull null
                        dto?.toDomain(originalId)
                    }
                    val entities = teachers.map { it.toEntity(isNextWeek) }
                    teacherDao.refreshTeachers(isNextWeek, entities)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Ignore to keep Room cache active
            }
        }

        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
            roomJob.cancel()
        }
    }

    override fun checkNextWeekTeachersAvailability(): Flow<Boolean> = callbackFlow {
        val roomJob = launch(Dispatchers.IO) {
            teacherDao.hasNextWeekTeachers().collect { count ->
                trySend(count > 0)
            }
        }

        val ref = db.getReference("teachers_next")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {}
            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
            roomJob.cancel()
        }
    }
}