package tj.msu.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [Index(value = ["uid"])]
)
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val date: String,
    val type: String,
    val timestamp: Long,
    val isRead: Boolean,
    val uid: String // Cache per user
)
