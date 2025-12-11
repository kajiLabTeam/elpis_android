package net.kajilab.elpissender.service

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kajilab.elpissender.R
import net.kajilab.elpissender.api.SharedPreferenceApi
import net.kajilab.elpissender.usecase.SensingUsecase

class SensingService : Service() {
    val sensingUsecase = SensingUsecase(this)
    val sharedPreferenceApi = SharedPreferenceApi()
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        sharedPreferenceApi.setBooleanValueByKey("isSensing", true, this)
        startForeground()
        serviceScope.launch {
            sensingUsecase.firstStart()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        sharedPreferenceApi.setBooleanValueByKey("isSensing", false, this)
        sensingUsecase.finalStop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startForeground() {
        // TODO: パーミッションチェックをしておくと良い
//        val locationPermission =
//            PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//        if (locationPermission != PermissionChecker.PERMISSION_GRANTED) {
//            stopSelf()
//            return
//        }

        try {
            val notification = createNotification()
            startForeground(1, notification)
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                e is ForegroundServiceStartNotAllowedException
            ) {
                Log.e("SensingService", "ForegroundServiceStartNotAllowedException occurred.")
            }
        }
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "SensingServiceChannel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    notificationChannelId,
                    "Sensing Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // 通知の内容を設定
        val notificationBuilder =
            NotificationCompat.Builder(this, notificationChannelId)
                .setContentTitle("Sensing Service")
                .setContentText("Service is running...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        return notificationBuilder.build()
    }
}
