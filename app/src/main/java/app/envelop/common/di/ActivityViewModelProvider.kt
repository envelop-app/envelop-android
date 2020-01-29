package app.envelop.common.di

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import javax.inject.Inject

@PerActivity
class ActivityViewModelProvider
@Inject constructor(
  activity: FragmentActivity,
  factory: ViewModelProvider.Factory
) {

  private val viewModelProvider: ViewModelProvider = ViewModelProvider(activity, factory)

  operator fun <T : ViewModel> get(viewModelClass: Class<T>): T {
    return viewModelProvider.get(viewModelClass)
  }
}
