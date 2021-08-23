package app.envelop.data

import android.app.Activity
import app.envelop.common.rx.rxSingleToOperation
import org.blockstack.android.sdk.BlockstackConnect

import org.blockstack.android.sdk.BlockstackSession
import javax.inject.Inject
import javax.inject.Provider

open class BlockstackLogin
@Inject constructor(
  private val blockstackProvider: Provider<BlockstackSession>,
  private val blockstackConnectProvider: Provider<BlockstackConnect>,
  private val activity: Activity
) {

  open fun login() =  blockstackConnectProvider.get().connect(activity, true)

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
