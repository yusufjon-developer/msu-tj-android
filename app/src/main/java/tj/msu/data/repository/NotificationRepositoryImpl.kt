package tj.msu.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.core.annotation.Single
import tj.msu.data.local.dao.NotificationDao
import tj.msu.data.mapper.toDomain
import tj.msu.data.mapper.toEntity
import tj.msu.data.model.NotificationDto
import tj.msu.domain.model.NotificationModel
import tj.msu.domain.repository.NotificationRepository

@Single
class NotificationRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val notificationDao: NotificationDao
) : NotificationRepository {

    override fun getNotifications(uid: String): Flow<List<NotificationModel>> = callbackFlow {
        val roomJob = launch(Dispatchers.IO) {
            notificationDao.getNotifications(uid).collect { entities ->
                trySend(entities.map { it.toDomain() })
            }
        }

        val collection = firestore.collection("users")
            .document(uid)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (snapshot != null) {
                launch(Dispatchers.IO) {
                    val notifications = snapshot.documents.mapNotNull { doc ->
                        val dto = doc.toObject(NotificationDto::class.java)
                        dto?.id = doc.id
                        dto?.toDomain()
                    }
                    val entities = notifications.map { it.toEntity(uid) }
                    notificationDao.refreshNotifications(uid, entities)
                }
            }
        }

        awaitClose {
            listener.remove()
            roomJob.cancel()
        }
    }

    override suspend fun markAsRead(uid: String, notificationId: String) {
        // Update local database first for instant UI response (optimistic UI pattern)
        try {
            notificationDao.updateReadStatus(notificationId, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Sync with Firestore remote server
        try {
            firestore.collection("users")
                .document(uid)
                .collection("notifications")
                .document(notificationId)
                .update("isRead", true)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}