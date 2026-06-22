package tj.msu.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import tj.msu.data.local.entity.FreeRoomsEntity

@Dao
interface FreeRoomsDao {

    @Query("SELECT COUNT(*) FROM free_rooms WHERE isNextWeek = 1")
    fun hasNextWeekFreeRooms(): Flow<Int>

    @Query("SELECT * FROM free_rooms WHERE isNextWeek = :isNextWeek")
    fun getFreeRooms(isNextWeek: Boolean): Flow<FreeRoomsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFreeRooms(freeRooms: FreeRoomsEntity)

    @Query("DELETE FROM free_rooms WHERE isNextWeek = :isNextWeek")
    suspend fun deleteFreeRooms(isNextWeek: Boolean)
}
