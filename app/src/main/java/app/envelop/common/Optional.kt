package app.envelop.common

/*
 * Based on https://medium.com/@davidcorsalini/ive-expanded-the-optional-sealed-class-a-bit-c30d329e0f0c
 */
sealed class Optional<out T> {
  class Some<out T>(val element: T) : Optional<T>()
  object None : Optional<Nothing>()

  fun element(): T? {
    return when (this) {
      is None -> null
      is Some -> element
    }
  }

  fun <K> map(mapper: ((T) -> K)): Optional<K> =
    when (this) {
      is Some -> create(mapper.invoke(element))
      is None -> None
    }

  override fun equals(other: Any?): Boolean {
    return this === other || (
        other is Optional<*> && (
            this is None && other is None
                || element()?.equals(other.element()) == true
            )
        )
  }

  override fun hashCode() = javaClass.hashCode()

  companion object {
    fun <T> create(element: T?) =
      element?.let { Some(it) } ?: None
  }
}