package app.envelop.common

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleSource

class Operation<out T>(
  private val result: T? = null,
  private val throwable: Throwable? = null
) {

  val isSuccessful get() = throwable == null
  val isError get() = !isSuccessful

  fun result() = result!!

  fun throwable() = throwable!!

  @Suppress("unused")
  fun <V> mapResult(map: ((T) -> V)) =
    Operation(
      result?.let { map.invoke(it) },
      throwable
    )

  val is404 get() = throwable().message?.contains("404") == true

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

fun <T, R> Single<Operation<T>>.mapIfSuccessful(mapper: ((T) -> R)): Single<Operation<R>> =
  map {
    if (it.isSuccessful) {
      Operation.success(mapper.invoke(it.result()))
    } else {
      Operation.error(it.throwable())
    }
  }

@Suppress("unused")
fun <T> Observable<Operation<T>>.doIfSuccessful(mapper: ((T) -> Unit)): Observable<Operation<T>> =
  doOnNext { if (it.isSuccessful) mapper.invoke(it.result()) }

fun <T> Observable<Operation<T>>.doIfError(mapper: ((Throwable) -> Unit)): Observable<Operation<T>> =
  doOnNext { if (it.isError) mapper.invoke(it.throwable()) }

fun <T> Single<Operation<T>>.doIfSuccessful(mapper: ((T) -> Unit)) =
  doOnSuccess { if (it.isSuccessful) mapper.invoke(it.result()) }

@Suppress("unused")
fun <T> Single<Operation<T>>.doIfError(mapper: ((Throwable) -> Unit)) =
  doOnSuccess { if (it.isError) mapper.invoke(it.throwable()) }

fun <T> Single<Operation<T>>.flatMapCompletableIfSuccessful(mapper: ((T) -> Completable)) =
  flatMap {
    if (it.isSuccessful) {
      mapper.invoke(it.result()).toSingleDefault(it)
    } else {
      Single.just(it)
    }
  }

fun <T> Single<T>.toOperation() =
  map { Operation.success(it) }
    .onErrorReturn { Operation.error(it) }