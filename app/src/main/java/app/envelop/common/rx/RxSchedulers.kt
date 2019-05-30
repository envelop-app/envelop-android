package app.envelop.common.rx

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

fun <T> Observable<T>.observeOnUI() =
  observeOn(AndroidSchedulers.mainThread())

fun <T> Single<T>.observeOnUI() =
  observeOn(AndroidSchedulers.mainThread())

fun <T> Maybe<T>.observeOnUI() =
  observeOn(AndroidSchedulers.mainThread())

fun Completable.observeOnUI() =
  observeOn(AndroidSchedulers.mainThread())

fun <T> Observable<T>.observeOnIO() =
  observeOn(Schedulers.io())

fun <T> Single<T>.observeOnIO() =
  observeOn(Schedulers.io())

fun <T> Maybe<T>.observeOnIO() =
  observeOn(Schedulers.io())

fun Completable.observeOnIO() =
  observeOn(Schedulers.io())
