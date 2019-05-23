package app.envelop.data.repositories

import app.envelop.common.Optional
import app.envelop.data.GsonPreferenceConverter
import app.envelop.data.models.User
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.google.gson.Gson
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Provider

class UserRepository
@Inject constructor(
  rxPreferencesProvider: Provider<RxSharedPreferences>,
  gson: Gson
) {

  private val userPreference by lazy {
    rxPreferencesProvider
      .get()
      .getObject(
        KEY_USER,
        Optional.None,
        GsonPreferenceConverter(gson, User::class)
      )
  }

  fun user() = userPreference.asObservable().subscribeOn(Schedulers.io())!!
  fun setUser(user: User?) = userPreference.set(Optional.create(user))

  companion object {
    private const val KEY_USER = "user"
  }
}