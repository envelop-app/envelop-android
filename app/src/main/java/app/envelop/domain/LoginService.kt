package app.envelop.domain

import android.app.Activity
import app.envelop.common.Operation
import app.envelop.common.di.PerActivity
import app.envelop.common.rx.rxSingleToOperation
import app.envelop.common.toOperation
import app.envelop.data.models.Profile
import app.envelop.data.models.User
import app.envelop.data.repositories.UserRepository
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxSingle
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.BlockstackSignIn
import org.blockstack.android.sdk.model.UserData
import javax.inject.Inject
import javax.inject.Provider

@PerActivity
class LoginService
@Inject constructor(
  private val blockstackProvider: Provider<BlockstackSession>,
  private val blockstackSignInProvider: Provider<BlockstackSignIn>,
  private val userRepository: UserRepository,
  private val activity: Activity
) {

  fun login() =
    rxSingleToOperation {
      blockstackSignInProvider.get().redirectUserToSignIn(activity)
    }

  fun finishLogin(response: String?): Single<Operation<Unit>> {
    if (response == null) {
      return Single.just(Operation.error(Error("Empty response")))
    }

    val authResponseTokens = response.split("/")
    if (authResponseTokens.size < 2) {
      return Single.just(Operation.error(Error("Invalid response")))
    }

    return rxSingle {
      val userData = blockstackProvider.get().handlePendingSignIn(authResponseTokens.last())
      if (userData.hasValue && userData.value.isComplete()) {
        userRepository.setUser(userData.value?.toUser())
      } else {
        throw Error(userData.error?.message)
      }
    }.toOperation()
  }

  private fun UserData?.isComplete() =
    this?.let {
      val username = it.json.optString("username")
      (!username.isNullOrBlank() && username != "null")
    } ?: false

  private fun UserData.toUser() =
    User(
      username = json.getString("username"),
      decentralizedId = decentralizedID,
      hubUrl = hubUrl,
      profile = profile?.let {
        Profile(
          name = it.name,
          description = it.description,
          avatarImage = it.avatarImage,
          email = it.email,
          isPerson = it.isPerson()
        )
      }
    )

  class Error(message: String?) : Exception(message)

}
