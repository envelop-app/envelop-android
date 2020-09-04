package app.envelop.common.rx

import app.envelop.common.Operation
import io.reactivex.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.rx2.rxSingle
import java.lang.Exception
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Creates cold [single][Single] that will run a given [block] in a coroutine and emits its result wrapped in [Operation]
 * The Value is wrapped so it can handle Exceptions even when those are launch after unsubscribing avoiding of propagation
 * Every time the returned observable is subscribed, it starts a new coroutine.
 * Unsubscribing cancels running coroutine.
 * Coroutine context can be specified with [context] argument.
 * If the context does not have any dispatcher nor any other [ContinuationInterceptor], then [Dispatchers.Default] is used.
 * Method throws [IllegalArgumentException] if provided [context] contains a [Job] instance.
 */
public fun <T : Any> rxSingleToOperation(
  context: CoroutineContext = EmptyCoroutineContext,
  block: suspend CoroutineScope.() -> T
):  Single<Operation<T>> {
   return rxSingle(context) {
     try {
       Operation.success(block.invoke(this))
     } catch (e:Exception) {
       return@rxSingle Operation.error<T>(e)
     }
   }
}
