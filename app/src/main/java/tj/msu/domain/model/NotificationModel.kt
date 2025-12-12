package tj.msu.domain.model

data class NotificationModel(
    val id: String,
    val title: String,
    val body: String,
    val date: String,
    val type: String,
    val timestamp: Long,
    var isRead: Boolean = false
)