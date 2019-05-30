package app.envelop.common

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.SingleSource

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
    fun success() = Operation(Unit)
    fun <T> success(result: T) = Operation(result = result)
    fun <T> error(throwable: Throwable) = Operation<T>(
      throwable = throwable
    )
  }
}

fun <T, R> Single<Operation<T>>.flatMapIfSuccessful(mapper: ((T) -> SingleSource<Operation<R>>)): Single<Operation<R>> =
  flatMap {
    if (it.isSuccessful) {
      mapper.invoke(it.result())
    } else {
      Single.just(Operation.error(it.throwable()))
    }
  }

fun <T> Single<Operation<T>>.doIfSuccessful(mapper: ((T) -> Unit)) =
  doOnSuccess { if (it.isSuccessful) mapper.invoke(it.result()) }

fun <T> Single<Operation<T>>.flatMapCompletableIfSuccessful(mapper: ((T) -> Completable)) =
  flatMap {
    if (it.isSuccessful) {
      mapper.invoke(it.result()).toSingleDefault(it)
    } else {
      Single.just(it)
    }
  }