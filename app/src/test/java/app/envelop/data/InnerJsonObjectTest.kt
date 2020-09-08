package app.envelop.data

import com.google.gson.JsonObject
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.days

class InnerJsonObjectTest {

  class TestJsonKey(
    override val value: String
  ) : JsonKey

  @Test
  fun listString() {
    val key = TestJsonKey("values")
    val value = listOf("AAA", "BBB", "CCC")
    val subject = InnerJsonObject(JsonObject())

    subject.set(key, value)
    assertEquals(value, subject.getListString(key))

    subject.set(key, null as List<String>?)
    assertNull(subject.optListString(key))
  }

  @Test
  fun emptyCreatedDateFallback() {
    val key = TestJsonKey("created_at")
    val subject = InnerJsonObject(JsonObject())
    assertNotNull(subject.getDate(key))
  }

  @Test
  fun getDate() {
    val key = TestJsonKey("created_at")
    val value = "2020-09-08T10:30:00"
    val subject = InnerJsonObject(JsonObject())

    subject.set(key, value)

    val result = subject.getDate(key)
    val resultCalendar =  Calendar.getInstance().also { it.time = result }

    assertEquals(8, resultCalendar.get(Calendar.DAY_OF_MONTH))
    assertEquals(8, resultCalendar.get(Calendar.MONTH))
    assertEquals(2020, resultCalendar.get(Calendar.YEAR))
    assertEquals(10, resultCalendar.get(Calendar.HOUR))
    assertEquals(30, resultCalendar.get(Calendar.MINUTE))
    assertEquals(0, resultCalendar.get(Calendar.SECOND))
  }

}
