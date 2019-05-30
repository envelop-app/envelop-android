package app.envelop.domain

import android.app.Activity
import app.envelop.common.Operation
import app.envelop.common.di.PerActivity
import app.envelop.data.models.Profile
import app.envelop.data.models.User
import app.envelop.data.repositories.UserRepository
import io.reactivex.Single
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.ISessionStore
import org.blockstack.android.sdk.model.BlockstackConfig
import org.blockstack.android.sdk.model.UserData
import javax.inject.Inject

@PerActivity
class LoginService
@Inject constructor(
  private val blockstackConfig: BlockstackConfig,
  private val blockstackSessionStore: ISessionStore,
  private val userRepository: UserRepository,
  private val activity: Activity
) {

  // Here we construct the BlockstackSession by hand because it needs the Activity
  private val blockstack by lazy {
    BlockstackSession(activity, blockstackConfig, sessionStore = blockstackSessionStore)
  }

  fun login() =
    Single
      .create<Operation<Unit>> { emitter ->
        blockstack.redirectUserToSignIn {
          emitter.onSuccess(Operation.error(Error(it.error)))
        }
      }

  fun finishLogin(response: String?) =
    Single
      .create<Operation<Unit>> { emitter ->
        if (response == null) {
          emitter.onSuccess(Operation.error(Error("Empty response")))
          return@create
        }

        val authResponseTokens = response.split("authResponse=", ignoreCase = true)
        if (authResponseTokens.size > 1) {
          blockstack.handlePendingSignIn(authResponseTokens.last()) { userData ->
            if (userData.hasValue) {
              userRepository.setUser(userData.value?.toUser())
              emitter.onSuccess(Operation.success(Unit))
            } else {
              emitter.onSuccess(Operation.error(Error(userData.error)))
            }
          }
        } else {
          emitter.onSuccess(Operation.error(Error("Invalid response")))
        }
      }

  fun cleanUp() {
    blockstack.release()
  }

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