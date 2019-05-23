package app.envelop.data.models

data class User(
  val decentralizedId: String,
  val hubUrl: String,
  val profile: Profile?
) {

  val displayName get() = profile?.name ?: profile?.email ?: decentralizedId

}