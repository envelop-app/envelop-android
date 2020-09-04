package app.envelop.data

import android.app.Activity
import app.envelop.common.rx.rxSingleToOperation

import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.BlockstackSignIn
import javax.inject.Inject
import javax.inject.Provider

open class BlockstackLogin
@Inject constructor(
  private val blockstackProvider: Provider<BlockstackSession>,
  private val blockstackSignInProvider: Provider<BlockstackSignIn>,
  private val activity: Activity
) {

  open fun redirectUserToSignIn() = rxSingleToOperation {
    blockstackSignInProvider.get().redirectUserToSignIn(activity)
  }

  open fun handlePendingSignIn(token: String) = rxSingleToOperation {
    val result = blockstackProvider.get().handlePendingSignIn(token)
    if (result.hasErrors || result.value == null) {
      throw Error(result.error?.message)
    } else {
      result.value!!
    }
  }

  class Error(message: String) : Exception(message)

}
