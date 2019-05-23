package app.envelop.domain

import app.envelop.common.Operation
import app.envelop.common.di.PerActivity
import app.envelop.data.models.Profile
import app.envelop.data.models.User
import app.envelop.data.repositories.UserRepository
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.model.UserData
import javax.inject.Inject

class UserService
@Inject constructor(
  private val userRepository: UserRepository
) {



}