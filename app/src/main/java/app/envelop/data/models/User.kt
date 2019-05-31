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
        ?: profile?.email?.ifBlank { null }
        ?: decentralizedId

  val usernameShort = username.replace(".id.blockstack", "")

}