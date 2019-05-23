package app.envelop.ui

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

abstract class BaseViewModel : ViewModel() {

  protected val disposables by lazy { CompositeDisposable() }

  public override fun onCleared() {
    super.onCleared()
    disposables.clear()
  }

}