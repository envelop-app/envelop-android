package app.envelop.data.models

data class User(
  val username: String,
  val decentralizedId: String,
  val hubUrl: String,
  val profile: Profile?
) {

  val displayName
    get() =
      profile?.name?.ifBlank { null }
        ?: usernameShort

  val usernameShort = usernameShort(username)

  companion object {
    fun usernameShort(username: String) =
      username.replace(".id.blockstack", "")
  }

}