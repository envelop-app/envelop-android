package app.envelop.ui.common

import android.app.Activity
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

object Click

fun PublishSubject<Click>.click() = onNext(Click)

object Refresh

fun PublishSubject<Refresh>.refresh() = onNext(Refresh)

data class Finish(
  val result: Result = Result.Canceled
) {
  enum class Result {
    Canceled, Ok
  }
}

fun Finish.Result.toActivityResult() =
  when (this) {
    Finish.Result.Canceled -> Activity.RESULT_CANCELED
    Finish.Result.Ok -> Activity.RESULT_OK
  }

fun PublishSubject<Finish>.finish(result: Finish.Result = Finish.Result.Canceled) = onNext(Finish(result))
fun BehaviorSubject<Finish>.finish(result: Finish.Result = Finish.Result.Canceled) = onNext(Finish(result))

object Open

fun PublishSubject<Open>.open() = onNext(Open)
fun BehaviorSubject<Open>.open() = onNext(Open)

sealed class LoadingState {
  object Loading : LoadingState()
  object Idle : LoadingState()
}

fun BehaviorSubject<LoadingState>.loading() = onNext(LoadingState.Loading)
fun BehaviorSubject<LoadingState>.idle() = onNext(LoadingState.Idle)

fun BehaviorSubject<Unit>.next() = onNext(Unit)
fun PublishSubject<Unit>.next() = onNext(Unit)