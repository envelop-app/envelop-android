package app.envelop.ui.common

import android.view.MenuItem
import android.view.View
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable

fun <T> Observable<T>.throttleForClicks() =
  throttleFirst(1, java.util.concurrent.TimeUnit.SECONDS)!!

fun View.clicksThrottled() =
  clicks().throttleForClicks()

@Suppress("unused")
fun MenuItem.clicksThrottled() =
  clicks().throttleForClicks()
