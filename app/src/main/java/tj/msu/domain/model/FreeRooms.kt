package tj.msu.domain.model

data class FreeRooms(
    val schedule: Map<String, Map<String, List<String>>> = emptyMap(),
    val lastUpdate: String = ""
)