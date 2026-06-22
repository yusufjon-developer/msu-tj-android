package tj.msu.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import tj.msu.data.local.dao.FreeRoomsDao
import tj.msu.data.local.dao.ScheduleDao
import tj.msu.data.mapper.toDomain
import tj.msu.data.mapper.toDomainList
import tj.msu.data.mapper.toEntity
import tj.msu.data.model.FreeRoomsResponseDto
import tj.msu.data.model.GroupScheduleDto
import tj.msu.domain.model.FreeRooms
import tj.msu.domain.model.Lesson
import tj.msu.domain.repository.ScheduleRepository

@Single
class ScheduleRepositoryImpl(
    private val database: FirebaseDatabase,
    private val scheduleDao: ScheduleDao,
    private val freeRoomsDao: FreeRoomsDao
) : ScheduleRepository {

    override fun getDailySchedule(groupId: String, isNextWeek: Boolean): Flow<List<Lesson>> = callbackFlow {
        val roomJob = launch(Dispatchers.IO) {
            scheduleDao.getLessons(groupId, isNextWeek).collect { entities ->
                trySend(entities.map { it.toDomain() })
            }
        }

        val path = if (isNextWeek) "schedules_next" else "schedules"
        val myRef = database.getReference(path).child(groupId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                launch(Dispatchers.IO) {
                    val dto = snapshot.getValue(GroupScheduleDto::class.java)
                    if (dto != null) {
                        val domainLessons = dto.toDomainList()
                        val entities = domainLessons.map { it.toEntity(groupId, isNextWeek) }
                        scheduleDao.refreshLessons(groupId, isNextWeek, entities)
                    } else {
                        scheduleDao.deleteLessons(groupId, isNextWeek)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Keep channel open so Room cache is still active and observed
            }
        }

        myRef.addValueEventListener(listener)

        awaitClose {
            myRef.removeEventListener(listener)
            roomJob.cancel()
        }
    }

    override fun getFreeRooms(isNextWeek: Boolean): Flow<FreeRooms> = callbackFlow {
        val roomJob = launch(Dispatchers.IO) {
            freeRoomsDao.getFreeRooms(isNextWeek).collect { entity ->
                trySend(entity?.toDomain() ?: FreeRooms())
            }
        }

        val path = if (isNextWeek) "free_rooms_next" else "free_rooms"
        val ref = database.getReference(path)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                launch(Dispatchers.IO) {
                    try {
                        val dto = snapshot.getValue(FreeRoomsResponseDto::class.java)
                        val domainModel = dto?.toDomain() ?: FreeRooms()
                        freeRoomsDao.insertFreeRooms(domainModel.toEntity(isNextWeek))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Keep channel open for Room cache
            }
        }

        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
            roomJob.cancel()
        }
    }

    override fun checkNextWeekScheduleAvailability(groupId: String): Flow<Boolean> = callbackFlow {
        val roomJob = launch(Dispatchers.IO) {
            scheduleDao.hasNextWeekLessons(groupId).collect { count ->
                trySend(count > 0)
            }
        }

        val ref = database.getReference("schedules_next").child(groupId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Just let Firebase update local RTDB or ignore, Room is source of truth for flow
                // But we can check if it exists in Firebase and trigger check.
                // Firebase listener is enough.
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
            roomJob.cancel()
        }
    }

    override fun checkNextWeekFreeRoomsAvailability(): Flow<Boolean> = callbackFlow {
        val roomJob = launch(Dispatchers.IO) {
            freeRoomsDao.hasNextWeekFreeRooms().collect { count ->
                trySend(count > 0)
            }
        }

        val ref = database.getReference("free_rooms_next")

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