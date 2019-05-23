package app.envelop.common.rx

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

fun <T> Observable<T>.observeOnUI() =
  observeOn(AndroidSchedulers.mainThread())

fun <T> Single<T>.observeOnUI() =
  observeOn(AndroidSchedulers.mainThread())

fun <T> Maybe<T>.observeOnUI() =
  observeOn(AndroidSchedulers.mainThread())

fun Completable.observeOnUI() =
  observeOn(AndroidSchedulers.mainThread())
