package app.envelop.data

import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.HandlerThread
import androidx.preference.PreferenceManager
import app.envelop.R
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.blockstack.android.sdk.*
import org.blockstack.android.sdk.model.BlockstackConfig
import org.blockstack.android.sdk.model.toBlockstackConfig
import javax.inject.Named
import javax.inject.Singleton

@Module
class BlockstackModule {

  @Provides
  fun blockstackConfig(resources: Resources) =
    resources.getString(R.string.blockstack_app_url)
      .toBlockstackConfig(arrayOf(Scope.StoreWrite, Scope.PublishData))

  @Provides
  @Singleton
  fun blockstackSessionStore(context: Context): ISessionStore =
    SessionStore(PreferenceManager.getDefaultSharedPreferences(context))

  @Provides
  @Singleton
  @Named("blockstack")
  fun blockstackScheduler(): Scheduler {
    val handlerThread = HandlerThread("BlockstackService").apply { start() }
    val handler = Handler(handlerThread.looper)
    return Schedulers.from {
      handler.post(it)
    }
  }

  @Provides
  @Singleton
  fun blockstackExecutor(context: Context, @Named("blockstack") scheduler: Scheduler): Executor =
    BlockstackExecutor(context, scheduler)


  @Provides
  @Singleton
  fun blockstackSession(context: Context, config: BlockstackConfig, sessionStore: ISessionStore, executor: Executor) =
    BlockstackSession(context, config, sessionStore = sessionStore, executor = executor)

}