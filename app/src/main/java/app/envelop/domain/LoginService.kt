package app.envelop.domain

import android.app.Activity
import app.envelop.common.Operation
import app.envelop.common.di.PerActivity
import app.envelop.common.mapIfSuccessful
import app.envelop.common.rx.rxSingleToOperation
import app.envelop.common.toOperation
import app.envelop.data.BlockstackLogin
import app.envelop.data.models.Profile
import app.envelop.data.models.User
import app.envelop.data.repositories.UserRepository
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxSingle
import org.blockstack.android.sdk.model.UserData
import javax.inject.Inject

@PerActivity
class LoginService
@Inject constructor(
  private val blockstackLogin: BlockstackLogin,
  private val userRepository: UserRepository
) {


  fun login() = blockstackLogin.redirectUserToSignIn()


  fun finishLogin(response: String?): Single<Operation<Unit>> {
    if (response == null) {
      return Single.just(Operation.error(Error("Empty response")))
    }

    val authResponseTokens = response.split("/")
    if (authResponseTokens.size < 2) {
      return Single.just(Operation.error(Error("Invalid response")))
    }

    return blockstackLogin.handlePendingSignIn(authResponseTokens.last())
      .mapIfSuccessful { userData ->
        if (userData.containsValidUsername()) {
          userRepository.setUser(userData.toUser())
        } else {
          throw UsernameMissing("Invalid Username")
        }
      }.onErrorReturn { Operation.error(it) }
  }

  private fun UserData?.containsValidUsername() =
    this?.json?.optString("username")?.let { username ->
      (!username.isBlank() && username != "null")
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
  class UsernameMissing(message: String?) : Exception(message)

}
