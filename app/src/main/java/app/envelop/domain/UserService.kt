package app.envelop.domain

import app.envelop.data.repositories.UserRepository
import javax.inject.Inject

class UserService
@Inject constructor(
  private val userRepository: UserRepository
) {

  fun user() =
    userRepository.user()

}