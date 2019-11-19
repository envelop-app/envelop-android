package app.envelop.ui.common

import android.app.Activity
import android.view.View
import androidx.core.view.isVisible
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

fun PublishSubject<Finish>.finish(result: Finish.Result = Finish.Result.Canceled) =
  onNext(Finish(result))

fun BehaviorSubject<Finish>.finish(result: Finish.Result = Finish.Result.Canceled) =
  onNext(Finish(result))

object Open

fun PublishSubject<Open>.open() = onNext(Open)

sealed class LoadingState {
  object Loading : LoadingState()
  object Idle : LoadingState()
}

fun BehaviorSubject<LoadingState>.loading() = onNext(LoadingState.Loading)
fun BehaviorSubject<LoadingState>.idle() = onNext(LoadingState.Idle)

sealed class VisibleState {
  object Visible : VisibleState()
  object Hidden : VisibleState()
}

fun BehaviorSubject<VisibleState>.next(isVisible: Boolean) =
  onNext(if (isVisible) VisibleState.Visible else VisibleState.Hidden)

fun View.setVisible(state: VisibleState) {
  isVisible = state == VisibleState.Visible
}

fun BehaviorSubject<Unit>.next() = onNext(Unit)
fun PublishSubject<Unit>.next() = onNext(Unit)
