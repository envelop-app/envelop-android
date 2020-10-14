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
import java.net.URI
import javax.inject.Named
import javax.inject.Singleton

@Module
class BlockstackModule {

  @Provides
  fun blockstackConfig(resources: Resources) =
    BlockstackConfig(
      URI(resources.getString(R.string.blockstack_app_url)),
      "/redirect",
      "/manifest.json",
      arrayOf(BaseScope.StoreWrite.scope, BaseScope.PublishData.scope)
    )

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
  fun blockstackSession(config: BlockstackConfig, sessionStore: ISessionStore) =
    BlockstackSession(sessionStore, config)

  @Provides
  @Singleton
  fun blockstackSignIn(config: BlockstackConfig, sessionStore: ISessionStore) =
    BlockstackSignIn(sessionStore, config)

  @Provides
  @Singleton
  fun blockstackConnect(config: BlockstackConfig, sessionStore: ISessionStore) =
    BlockstackConnect.config(config, sessionStore)
}
