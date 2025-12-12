package tj.msu.data.model

import com.google.firebase.firestore.PropertyName

data class NotificationDto(
    var id: String = "",
    val title: String = "",
    val body: String = "",
    val timestamp: Long = 0,
    val type: String = "info",
    @get:PropertyName("isRead") @set:PropertyName("isRead")
    var isRead: Boolean = false
)