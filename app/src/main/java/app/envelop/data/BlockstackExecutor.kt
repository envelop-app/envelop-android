package app.envelop.data

import android.content.Context
import io.reactivex.Scheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.blockstack.android.sdk.Executor
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class BlockstackExecutor
@Inject constructor(
  private val context: Context,
  @Named("blockstack") private val scheduler: Scheduler
) : Executor {

  override fun onV8Thread(function: () -> Unit) {
    scheduler.scheduleDirect {
      try {
        function.invoke()
      } catch (e: Exception) {
        Timber.e(e, "onV8Thread")
      }
    }
  }

  override fun onMainThread(function: (ctx: Context) -> Unit) {
    GlobalScope.launch(Dispatchers.Main) {
      function(context)
    }
  }

  override fun onNetworkThread(function: suspend () -> Unit) {
    GlobalScope.launch(Dispatchers.IO) {
      try {
        function()
      } catch (e: Exception) {
        Timber.e(e, "onNetworkThread")
      }
    }
  }

}