package app.envelop.data

import androidx.room.TypeConverter
import java.util.*

class Converters {
  @TypeConverter
  fun timestampToDate(value: Long?) = value?.let { Date(value) }

  @TypeConverter
  fun dateToTimestamp(date: Date?) = date?.time
}