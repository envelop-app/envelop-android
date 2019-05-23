package app.envelop.ui.common

import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

object Click

fun PublishSubject<Click>.click() = onNext(Click)

object Finish

fun PublishSubject<Finish>.finish() = onNext(Finish)
fun BehaviorSubject<Finish>.finish() = onNext(Finish)

sealed class LoadingState {
  object Loading : LoadingState()
  object Idle : LoadingState()
}

fun BehaviorSubject<LoadingState>.loading() = onNext(LoadingState.Loading)
fun BehaviorSubject<LoadingState>.idle() = onNext(LoadingState.Idle)

fun BehaviorSubject<Boolean>.on() = onNext(true)
fun BehaviorSubject<Boolean>.off() = onNext(false)

fun BehaviorSubject<Unit>.next() = onNext(Unit)
fun PublishSubject<Unit>.next() = onNext(Unit)