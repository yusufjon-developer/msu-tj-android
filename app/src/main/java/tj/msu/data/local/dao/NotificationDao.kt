package tj.msu.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import tj.msu.data.local.entity.NotificationEntity

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications WHERE uid = :uid ORDER BY timestamp DESC")
    fun getNotifications(uid: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = :isRead WHERE id = :notificationId")
    suspend fun updateReadStatus(notificationId: String, isRead: Boolean)

    @Query("DELETE FROM notifications WHERE uid = :uid")
    suspend fun deleteNotificationsForUser(uid: String)

    @Transaction
    suspend fun refreshNotifications(uid: String, notifications: List<NotificationEntity>) {
        deleteNotificationsForUser(uid)
        insertNotifications(notifications)
    }
}
