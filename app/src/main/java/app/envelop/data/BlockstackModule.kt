package app.envelop.data

import android.content.Context
import android.content.res.Resources
import android.preference.PreferenceManager
import app.envelop.R
import dagger.Module
import dagger.Provides
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.ISessionStore
import org.blockstack.android.sdk.Scope
import org.blockstack.android.sdk.SessionStore
import org.blockstack.android.sdk.model.BlockstackConfig
import org.blockstack.android.sdk.model.toBlockstackConfig
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
  fun blockstackSession(context: Context, config: BlockstackConfig, sessionStore: ISessionStore) =
    BlockstackSession(context, config, sessionStore = sessionStore)

}