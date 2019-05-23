package app.envelop.domain

import android.app.Activity
import app.envelop.common.Operation
import app.envelop.common.di.PerActivity
import io.reactivex.Single
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.model.BlockstackConfig
import javax.inject.Inject

@PerActivity
class LoginService
@Inject constructor(
  private val blockstackConfig: BlockstackConfig,
  private val activity: Activity
) {

  // Here we construct the BlockstackSession by hand because it needs the Activity
  private val blockstack by lazy {
    BlockstackSession(activity, blockstackConfig)
  }

  fun login() =
    Single
      .create<Operation<Unit>> { emitter ->
        blockstack.redirectUserToSignIn {
          emitter.onSuccess(Operation.error(Error(it.error)))
        }
      }

  fun cleanUp() {
    blockstack.release()
  }

  class Error(message: String?) : Exception(message)

}