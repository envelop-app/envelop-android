package app.envelop.data

import app.envelop.common.Optional
import com.f2prateek.rx.preferences2.Preference
import com.google.gson.Gson
import kotlin.reflect.KClass

class GsonPreferenceConverter<T : Any>(
  private val gson: Gson,
  private val klass: KClass<T>
) : Preference.Converter<Optional<T>> {

  override fun deserialize(serialized: String): Optional<T> =
    Optional.create(gson.fromJson(serialized, klass.java))

  override fun serialize(value: Optional<T>): String =
    gson.toJson(value.element())

}