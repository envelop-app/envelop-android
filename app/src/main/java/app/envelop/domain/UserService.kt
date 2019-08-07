package app.envelop.domain

import app.envelop.common.Optional
import app.envelop.data.repositories.UserRepository
import javax.inject.Inject

class UserService
@Inject constructor(
  private val userRepository: UserRepository
) {

  fun user() =
    userRepository.user()

  fun userSingle() =
    userRepository
      .user()
      .filter { it is Optional.Some }
      .map { it.element()!! }
      .take(1)
      .firstOrError()

}