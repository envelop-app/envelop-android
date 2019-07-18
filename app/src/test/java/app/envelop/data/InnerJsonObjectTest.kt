package app.envelop.data

import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

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
}