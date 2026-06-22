package tj.msu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import tj.msu.data.local.converters.RoomConverters
import tj.msu.data.local.dao.FreeRoomsDao
import tj.msu.data.local.dao.NotificationDao
import tj.msu.data.local.dao.ScheduleDao
import tj.msu.data.local.dao.TeacherDao
import tj.msu.data.local.entity.FreeRoomsEntity
import tj.msu.data.local.entity.LessonEntity
import tj.msu.data.local.entity.NotificationEntity
import tj.msu.data.local.entity.TeacherEntity

@Database(
    entities = [
        LessonEntity::class,
        FreeRoomsEntity::class,
        TeacherEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
    abstract fun freeRoomsDao(): FreeRoomsDao
    abstract fun teacherDao(): TeacherDao
    abstract fun notificationDao(): NotificationDao
}
