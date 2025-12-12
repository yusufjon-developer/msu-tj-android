package tj.msu.data.mapper

import tj.msu.data.model.NotificationDto
import tj.msu.domain.model.NotificationModel
import java.text.SimpleDateFormat
import java.util.*

fun NotificationDto.toDomain(): NotificationModel {
    val date = Date(this.timestamp)
    val format = SimpleDateFormat("dd MMM, HH:mm", Locale("ru"))

    return NotificationModel(
        id = this.id,
        title = this.title,
        body = this.body,
        date = format.format(date),
        type = this.type,
        timestamp = this.timestamp,
        isRead = this.isRead
    )
}