package app.envelop.background

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import app.envelop.App
import app.envelop.R
import app.envelop.data.models.UploadState
import app.envelop.domain.UploadService
import app.envelop.ui.main.MainActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class UploadBackgroundService : Service() {

  @Inject
  lateinit var uploadService: UploadService

  private val notificationManager by lazy {
    this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
  }

  private val pendingIntent by lazy {
    MainActivity.getIntent(this).let {
      PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_UPDATE_CURRENT)
    }
  }

  private val disposables = CompositeDisposable()
  private var onForeground = false

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    uploadService.startUpload()

    // If we get killed, after returning from here, restart
    return START_STICKY
  }

  override fun onCreate() {
    (applicationContext as App).component.inject(this)

    uploadService
      .state()
      .subscribe {
        when (it) {
          is UploadState.Idle -> {
            if (onForeground) stopForeground(true)
            onForeground = false
          }
          is UploadState.Uploading -> {
            val notification = buildNotification(it)
            if (!onForeground) {
              startForeground(UPLOAD_NOTIFICATION_ID, notification)
              onForeground = true
            } else {
              notificationManager.notify(UPLOAD_NOTIFICATION_ID, notification)
            }
          }
        }
      }
      .addTo(disposables)
  }

  override fun onDestroy() {
    super.onDestroy()
    uploadService.stopUpload()
    disposables.clear()
  }

  override fun onBind(intent: Intent): Nothing? = null

  private fun buildNotification(state: UploadState.Uploading) =
    getNotificationBuilder()
      .setContentTitle(
        resources.getQuantityString(
          R.plurals.uploading_files_notification,
          state.fileCount,
          state.fileCount
        )
      )
      .setContentText(state.nextUpload.name)
      .setSmallIcon(R.drawable.notification_icon)
      .setContentIntent(pendingIntent)
      .setProgress(100, state.nextUpload.progress.percentage, false)
      .setColor(ResourcesCompat.getColor(resources, R.color.primary, theme))
      .build()

  private fun getNotificationBuilder() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationCompat.Builder(this, getOrCreateChannel().id)
    } else {
      @Suppress("DEPRECATION")
      NotificationCompat.Builder(this)
    }

  @TargetApi(Build.VERSION_CODES.O)
  private fun getOrCreateChannel() =
    notificationManager.getNotificationChannel(UPLOAD_CHANNEL_ID)
      ?: NotificationChannel(
        UPLOAD_CHANNEL_ID,
        getString(R.string.upload_notification_channel),
        NotificationManager.IMPORTANCE_LOW
      ).also {
        it.description = getString(R.string.upload_notification_channel_description)
        notificationManager.createNotificationChannel(it)
      }

  companion object {
    private const val UPLOAD_CHANNEL_ID = "upload"
    private const val UPLOAD_NOTIFICATION_ID = 21

    fun getIntent(context: Context) = Intent(context, UploadBackgroundService::class.java)
  }
}
