package tj.msu.data.model

import com.google.firebase.database.PropertyName

data class FreeRoomsResponseDto(
    @get:PropertyName("schedule") @set:PropertyName("schedule")
    var schedule: Any? = null,

    @get:PropertyName("last_update") @set:PropertyName("last_update")
    var lastUpdate: String? = null
)