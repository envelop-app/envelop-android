package app.envelop.data

import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.HandlerThread
import android.preference.PreferenceManager
import app.envelop.R
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.blockstack.android.sdk.BaseScope
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.ISessionStore
import org.blockstack.android.sdk.SessionStore
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
    fun blockstackSession(context: Context, config: BlockstackConfig, sessionStore: ISessionStore) =
        BlockstackSession(appConfig = config, sessionStore = sessionStore)

}