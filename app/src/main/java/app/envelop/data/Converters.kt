package app.envelop.data

import androidx.room.TypeConverter
import java.util.*

class Converters {
  @TypeConverter
  fun timestampToDate(value: Long?) = value?.let { Date(value) }

  @TypeConverter
  fun dateToTimestamp(date: Date?) = date?.time

  @TypeConverter
  fun stringToIntList(value: String?) =
    value?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()

  @TypeConverter
  fun intListToString(list: List<Int>?) =
    list?.joinToString(",") ?: ""

  @TypeConverter
  fun stringToStringList(value: String?) =
    value?.split(",")

  @TypeConverter
  fun stringListToString(list: List<String>?) =
    list?.joinToString(",")
}