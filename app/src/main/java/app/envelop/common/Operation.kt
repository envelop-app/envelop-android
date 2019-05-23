package app.envelop.common

class Operation<T>(
  private val result: T? = null,
  private val throwable: Throwable? = null
) {

  val isSuccessful get() = throwable == null
  val isError get() = !isSuccessful

  fun result() = result!!

  fun throwable() = throwable!!

  fun <V> mapResult(map: ((T) -> V)) =
    Operation(
      result?.let { map.invoke(it) },
      throwable
    )

  companion object {
    fun <T> success(result: T) = Operation(result = result)
    fun <T> error(throwable: Throwable) = Operation<T>(
      throwable = throwable
    )
  }
}