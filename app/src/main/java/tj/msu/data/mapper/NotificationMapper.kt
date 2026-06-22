package tj.msu.data.mapper

import tj.msu.data.local.entity.NotificationEntity
import tj.msu.data.model.NotificationDto
import tj.msu.domain.model.NotificationModel
import java.text.SimpleDateFormat
import java.util.*

fun NotificationDto.toDomain(): NotificationModel {
    val date = Date(this.timestamp)
    val format = SimpleDateFormat("dd MMM, HH:mm", Locale("ru"))

    return NotificationModel(
        id = this.id ?: "",
        title = this.title ?: "",
        body = this.body ?: "",
        date = format.format(date),
        type = this.type ?: "",
        timestamp = this.timestamp,
        isRead = this.isRead
    )
}

fun NotificationEntity.toDomain(): NotificationModel {
    return NotificationModel(
        id = this.id,
        title = this.title,
        body = this.body,
        date = this.date,
        type = this.type,
        timestamp = this.timestamp,
        isRead = this.isRead
    )
}

fun NotificationModel.toEntity(uid: String): NotificationEntity {
    return NotificationEntity(
        id = this.id,
        title = this.title,
        body = this.body,
        date = this.date,
        type = this.type,
        timestamp = this.timestamp,
        isRead = this.isRead,
        uid = uid
    )
}