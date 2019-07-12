package app.envelop.data

import androidx.room.TypeConverter
import java.util.*

class Converters {
  @TypeConverter
  fun timestampToDate(value: Long?) = value?.let { Date(value) }

  @TypeConverter
  fun dateToTimestamp(date: Date?) = date?.time

  @TypeConverter
  fun stringToListInt(value: String?) =
    value?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()

  @TypeConverter
  fun listIntToString(list: List<Int>?) =
    list?.joinToString(",") ?: ""
}