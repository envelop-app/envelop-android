package app.envelop.data

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import app.envelop.common.rx.rxSingleToOperation

import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.BlockstackSignIn
import org.blockstack.android.sdk.ui.showBlockstackConnect
import javax.inject.Inject
import javax.inject.Provider

open class BlockstackLogin
@Inject constructor(
  private val blockstackProvider: Provider<BlockstackSession>,
  public val blockstackSignInProvider: Provider<BlockstackSignIn>,
  private val activity: AppCompatActivity
) {

  open fun redirectUserToSignIn() = rxSingleToOperation {
    //think this is related with AppCompatActivity and SignInProvider
    activity.showBlockstackConnect()
    //blockstackSignInProvider.get().redirectUserToSignIn(activity)
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
