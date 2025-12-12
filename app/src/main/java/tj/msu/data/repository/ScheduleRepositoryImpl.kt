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
import tj.msu.data.mapper.toDomainList
import tj.msu.data.model.FreeRoomsResponseDto
import tj.msu.data.model.GroupScheduleDto
import tj.msu.domain.model.FreeRooms
import tj.msu.domain.model.Lesson
import tj.msu.domain.repository.ScheduleRepository

@Single
class ScheduleRepositoryImpl : ScheduleRepository {

    private val database = FirebaseDatabase.getInstance()

    override fun getDailySchedule(groupId: String): Flow<List<Lesson>> = callbackFlow {
        val myRef = database.getReference("schedules").child(groupId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dto = snapshot.getValue(GroupScheduleDto::class.java)
                if (dto != null) {
                    trySend(dto.toDomainList())
                } else {
                    trySend(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        myRef.addValueEventListener(listener)

        awaitClose { myRef.removeEventListener(listener) }
    }

    override fun getFreeRooms(): Flow<FreeRooms> = callbackFlow {
        val ref = database.getReference("free_rooms")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val dto = snapshot.getValue(FreeRoomsResponseDto::class.java)
                    val domainModel = dto?.toDomain() ?: FreeRooms()
                    trySend(domainModel)
                } catch (e: Exception) {
                    trySend(FreeRooms())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}