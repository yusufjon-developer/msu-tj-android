package tj.msu.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import tj.msu.data.local.entity.LessonEntity

@Dao
interface ScheduleDao {

    @Query("SELECT COUNT(*) FROM lessons WHERE groupId = :groupId AND isNextWeek = 1")
    fun hasNextWeekLessons(groupId: String): Flow<Int>

    @Query("SELECT * FROM lessons WHERE groupId = :groupId AND isNextWeek = :isNextWeek ORDER BY dayIndex ASC, time ASC")
    fun getLessons(groupId: String, isNextWeek: Boolean): Flow<List<LessonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Query("DELETE FROM lessons WHERE groupId = :groupId AND isNextWeek = :isNextWeek")
    suspend fun deleteLessons(groupId: String, isNextWeek: Boolean)

    @Transaction
    suspend fun refreshLessons(groupId: String, isNextWeek: Boolean, lessons: List<LessonEntity>) {
        deleteLessons(groupId, isNextWeek)
        insertLessons(lessons)
    }
}
