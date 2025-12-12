package tj.msu.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.msu.domain.model.NotificationModel

interface NotificationRepository {
    fun getNotifications(uid: String): Flow<List<NotificationModel>>
    suspend fun markAsRead(uid: String, notificationId: String)
}