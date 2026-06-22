package tj.msu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import tj.msu.data.local.entity.TeacherEntity

@Dao
interface TeacherDao {

    @Query("SELECT COUNT(*) FROM teachers WHERE isNextWeek = 1")
    fun hasNextWeekTeachers(): Flow<Int>

    @Query("SELECT * FROM teachers WHERE isNextWeek = :isNextWeek ORDER BY name ASC")
    fun getTeachers(isNextWeek: Boolean): Flow<List<TeacherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<TeacherEntity>)

    @Query("DELETE FROM teachers WHERE isNextWeek = :isNextWeek")
    suspend fun deleteTeachers(isNextWeek: Boolean)

    @Transaction
    suspend fun refreshTeachers(isNextWeek: Boolean, teachers: List<TeacherEntity>) {
        deleteTeachers(isNextWeek)
        insertTeachers(teachers)
    }
}
