package app.envelop.domain

import app.envelop.common.rx.observeOnUI
import app.envelop.data.repositories.DocRepository
import app.envelop.data.repositories.UploadRepository
import app.envelop.data.repositories.UserRepository
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.blockstack.android.sdk.BlockstackSession
import javax.inject.Inject
import javax.inject.Provider

class LogoutService
@Inject constructor(
  private val blockstackProvider: Provider<BlockstackSession>,
  private val userRepository: UserRepository,
  private val docRepository: DocRepository,
  private val uploadRepository: UploadRepository
) {

  private val blockstack by lazy {
    blockstackProvider.get()
  }

  fun logout() =
    Completable
      .fromAction {
        docRepository.deleteAll()
        uploadRepository.deleteAll()
        userRepository.setUser(null)
      }
      .subscribeOn(Schedulers.io())
      .observeOnUI()
      .doOnComplete { blockstack.signUserOut() }

}