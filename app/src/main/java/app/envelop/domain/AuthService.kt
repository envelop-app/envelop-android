package app.envelop.domain

import app.envelop.common.Operation
import app.envelop.data.models.Profile
import app.envelop.data.models.User
import app.envelop.data.repositories.UserRepository
import io.reactivex.Completable
import io.reactivex.Single
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.model.UserData
import javax.inject.Inject

class AuthService
@Inject constructor(
  private val blockstack: BlockstackSession,
  private val userRepository: UserRepository
) {

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

  fun logout() =
    Completable.fromAction {
      userRepository.setUser(null)
      blockstack.signUserOut()
    }

  fun user() =
    userRepository.user()

  private fun UserData.toUser() =
    User(
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