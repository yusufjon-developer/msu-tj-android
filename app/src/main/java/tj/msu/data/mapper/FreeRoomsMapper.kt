package tj.msu.data.mapper

import tj.msu.data.model.FreeRoomsResponseDto
import tj.msu.domain.model.FreeRooms

fun FreeRoomsResponseDto.toDomain(): FreeRooms {
    return FreeRooms(
        schedule = parseSchedule(this.schedule),
        lastUpdate = this.lastUpdate ?: ""
    )
}

private fun parseSchedule(scheduleData: Any?): Map<String, Map<String, List<String>>> {
    val result = mutableMapOf<String, Map<String, List<String>>>()

    when (scheduleData) {
        is List<*> -> {
            scheduleData.forEachIndexed { dayIndex, dayData ->
                if (dayData != null) {
                    val dayKey = dayIndex.toString()
                    result[dayKey] = parseDayData(dayData)
                }
            }
        }
        is Map<*, *> -> {
            scheduleData.forEach { (key, value) ->
                val dayKey = key.toString()
                result[dayKey] = parseDayData(value)
            }
        }
    }
    return result
}

private fun parseDayData(dayData: Any?): Map<String, List<String>> {
    val pairMap = mutableMapOf<String, List<String>>()

    when (dayData) {
        is List<*> -> {
            dayData.forEachIndexed { pairIndex, roomsData ->
                if (roomsData != null) {
                    val pairKey = pairIndex.toString()
                    pairMap[pairKey] = parseRoomsList(roomsData)
                }
            }
        }
        is Map<*, *> -> {
            dayData.forEach { (key, value) ->
                val pairKey = key.toString()
                pairMap[pairKey] = parseRoomsList(value)
            }
        }
    }
    return pairMap
}

private fun parseRoomsList(roomsData: Any?): List<String> {
    return when (roomsData) {
        is List<*> -> roomsData.map { it.toString() }
        else -> emptyList()
    }
}