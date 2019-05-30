package app.envelop.data.models

data class User(
  val username: String,
  val decentralizedId: String,
  val hubUrl: String,
  val profile: Profile?
) {

  val displayName get() = profile?.name ?: profile?.email ?: decentralizedId
  val usernameShort = username.replace(".id.blockstack", "")

}