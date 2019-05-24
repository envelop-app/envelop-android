package app.envelop.ui.common

import android.content.Intent

data class ActivityResult(
  val requestCode: Int,
  val resultCode: Int,
  val intent: Intent?
)