package app.envelop.domain

import app.envelop.data.repositories.DocRepository
import app.envelop.data.repositories.UploadRepository
import app.envelop.data.repositories.UserRepository
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.blockstack.android.sdk.BlockstackSession
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider

class LogoutService
@Inject constructor(
  private val blockstackProvider: Provider<BlockstackSession>,
  @Named("blockstack") private val blockstackScheduler: Scheduler,
  private val userRepository: UserRepository,
  private val docRepository: DocRepository,
  private val uploadRepository: UploadRepository
) {

  private val blockstack by lazy {
    blockstackProvider.get()
  }

  fun logout(): Completable =

    Completable
      .fromAction {
        uploadRepository.deleteAll()
        userRepository.setUser(null)
      }
      .andThen(docRepository.deleteAll())
      .subscribeOn(Schedulers.io())
      .observeOn(blockstackScheduler)
      .doOnComplete { blockstack.signUserOut() }

}