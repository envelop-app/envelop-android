package app.envelop.common.rx

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

fun <T> Observable<T>.observeOnUI(): Observable<T> =
  observeOn(AndroidSchedulers.mainThread())

fun <T> Single<T>.observeOnUI() =
  observeOn(AndroidSchedulers.mainThread())

fun <T> Single<T>.observeOnIO() =
  observeOn(Schedulers.io())

