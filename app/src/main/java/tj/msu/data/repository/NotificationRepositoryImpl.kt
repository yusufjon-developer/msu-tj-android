package tj.msu.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.koin.core.annotation.Single
import tj.msu.data.mapper.toDomain
import tj.msu.data.model.NotificationDto
import tj.msu.domain.model.NotificationModel
import tj.msu.domain.repository.NotificationRepository

@Single
class NotificationRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    override fun getNotifications(uid: String): Flow<List<NotificationModel>> = callbackFlow {
       
       
        val collection = firestore.collection("users")
            .document(uid)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val notifications = snapshot.documents.mapNotNull { doc ->
                   
                    val dto = doc.toObject(NotificationDto::class.java)
                    dto?.id = doc.id
                    dto?.toDomain()
                }
                trySend(notifications)
            }
        }

        awaitClose { listener.remove() }
    }

    override suspend fun markAsRead(uid: String, notificationId: String) {
       
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