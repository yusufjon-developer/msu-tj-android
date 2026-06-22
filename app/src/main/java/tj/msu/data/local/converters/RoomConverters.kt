package tj.msu.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import tj.msu.domain.model.DayScheduleModel

class RoomConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) return null
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromFreeRoomsMap(value: Map<String, Map<String, List<String>>>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toFreeRoomsMap(value: String?): Map<String, Map<String, List<String>>>? {
        if (value == null) return null
        val type = object : TypeToken<Map<String, Map<String, List<String>>>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromDayScheduleList(value: List<DayScheduleModel>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toDayScheduleList(value: String?): List<DayScheduleModel>? {
        if (value == null) return null
        val type = object : TypeToken<List<DayScheduleModel>>() {}.type
        return gson.fromJson(value, type)
    }
}
