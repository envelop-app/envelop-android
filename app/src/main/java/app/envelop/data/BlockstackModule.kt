package app.envelop.data

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import app.envelop.R
import app.envelop.common.di.PerActivity
import dagger.Module
import dagger.Provides
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.Scope
import org.blockstack.android.sdk.model.BlockstackConfig
import org.blockstack.android.sdk.model.toBlockstackConfig
import javax.inject.Named

@Module
class BlockstackModule {

  @Provides
  fun blockstackConfig(resources: Resources) =
    resources.getString(R.string.blockstack_app_url)
      .toBlockstackConfig(arrayOf(Scope.StoreWrite, Scope.PublishData))

  @Provides
  fun blockstackSession(context: Context, config: BlockstackConfig) =
    BlockstackSession(context, config)

}