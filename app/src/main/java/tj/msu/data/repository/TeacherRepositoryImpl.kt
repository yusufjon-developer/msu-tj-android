package tj.msu.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.annotation.Single
import tj.msu.data.mapper.toDomain
import tj.msu.data.model.TeacherDto
import tj.msu.domain.model.TeacherModel
import tj.msu.domain.repository.TeacherRepository

@Single
class TeacherRepositoryImpl(
    private val db: FirebaseDatabase
) : TeacherRepository {

    override fun getTeachers(isNextWeek: Boolean): Flow<List<TeacherModel>> = callbackFlow {
        val path = if (isNextWeek) "teachers_next" else "teachers"
        val ref = db.getReference(path)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val teachers = snapshot.children.mapNotNull { child ->

                    val dto = child.getValue(TeacherDto::class.java)
                    val originalId = child.key ?: return@mapNotNull null


                    dto?.toDomain(originalId)
                }.sortedBy { it.name }

                trySend(teachers)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override fun checkNextWeekTeachersAvailability(): Flow<Boolean> = callbackFlow {
        val ref = db.getReference("teachers_next")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}