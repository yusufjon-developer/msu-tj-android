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

    override fun getDailySchedule(groupId: String, isNextWeek: Boolean): Flow<List<Lesson>> = callbackFlow {
        val path = if (isNextWeek) "schedules_next" else "schedules"
        val myRef = database.getReference(path).child(groupId)

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

    override fun getFreeRooms(isNextWeek: Boolean): Flow<FreeRooms> = callbackFlow {
        val path = if (isNextWeek) "free_rooms_next" else "free_rooms"
        val ref = database.getReference(path)

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

    override fun checkNextWeekScheduleAvailability(groupId: String): Flow<Boolean> = callbackFlow {
        val ref = database.getReference("schedules_next").child(groupId)

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

    override fun checkNextWeekFreeRoomsAvailability(): Flow<Boolean> = callbackFlow {
        val ref = database.getReference("free_rooms_next")

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