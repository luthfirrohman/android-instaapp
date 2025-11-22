package id.app.instaapp.data.local

import androidx.room.TypeConverter
import org.json.JSONArray

class PostConverters {
    @TypeConverter
    fun fromAllowedUsers(value: List<String>?): String {
        val jsonArray = JSONArray()
        value.orEmpty().forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toAllowedUsers(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        val source = JSONArray(value)
        val result = mutableListOf<String>()
        for (i in 0 until source.length()) {
            result.add(source.optString(i))
        }
        return result
    }
}