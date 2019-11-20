package app.envelop.domain

import android.app.Activity
import app.envelop.common.Operation
import app.envelop.common.di.PerActivity
import app.envelop.data.models.Profile
import app.envelop.data.models.User
import app.envelop.data.repositories.UserRepository
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.BlockstackSignIn
import org.blockstack.android.sdk.ISessionStore
import org.blockstack.android.sdk.model.BlockstackConfig
import org.blockstack.android.sdk.model.UserData
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider

@PerActivity
class LoginService
@Inject constructor(
  private val blockstackProvider: Provider<BlockstackSession>,
  private val blockstackConfig: BlockstackConfig,
  private val blockstackSessionStore: ISessionStore,
  @Named("blockstack") private val blockstackScheduler: Scheduler,
  private val userRepository: UserRepository,
  private val activity: Activity
) {

  private val blockstack by lazy {
    blockstackProvider.get()
  }

  private val blockstackSignIn by lazy {
    BlockstackSignIn(
      appConfig = blockstackConfig,
      sessionStore = blockstackSessionStore
    )
  }

  fun login() =
    Single
      .create<Operation<Unit>> { emitter ->
        CoroutineScope(Dispatchers.Main).launch {
            blockstackSignIn.redirectUserToSignIn(activity)
            emitter.onSuccess(Operation.error(Error(null)))
        }
      }
      .subscribeOn(blockstackScheduler)

  fun finishLogin(response: String?) =
    Single
      .create<Operation<Unit>> { emitter ->
        if (response == null) {
          emitter.onSuccess(Operation.error(Error("Empty response")))
          return@create
        }

        val authResponseTokens = response.split("/")
        if (authResponseTokens.size > 1) {
          CoroutineScope(Dispatchers.IO).launch {
            val userData = blockstack.handlePendingSignIn(authResponseTokens.last())
            if (userData.hasValue && userData.value.isComplete()) {
              userRepository.setUser(userData.value?.toUser())
              emitter.onSuccess(Operation.success(Unit))
            } else {
              emitter.onSuccess(Operation.error(Error(userData.error!!.message)))
            }
          }
        } else {
          emitter.onSuccess(Operation.error(Error("Invalid response")))
        }
      }
      .subscribeOn(blockstackScheduler)

  fun cleanUp() =
    Completable
      .fromAction {
        // nothing to clean up ?
      }
      .subscribeOn(blockstackScheduler)

  private fun UserData?.isComplete() =
    this?.let {
      !it.json.optString("username").isNullOrBlank()
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