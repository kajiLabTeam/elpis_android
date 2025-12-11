package net.kajilab.elpissender.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kajilab.elpissender.R
import net.kajilab.elpissender.api.SharedPreferenceApi
import net.kajilab.elpissender.repository.LogSendRepository
import net.kajilab.elpissender.usecase.SensingUsecase

class SensingWorker(
    val context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {
    companion object {
        const val WORKER_TAG = "sensing_worker"
    }

    private val notificationId = 1

    private val sensingUsecase = SensingUsecase(context)
    private val sharedPreferenceApi = SharedPreferenceApi()
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val logSendRepository = LogSendRepository()

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            notificationId,
            createNotification(),
        )
    }

    override suspend fun doWork(): Result {
        sharedPreferenceApi.setBooleanValueByKey("isSensing", true, context)
        serviceScope.launch {
            logSendRepository.sendLog("start", "センシングを開始しました", 1)
            sensingUsecase.timerStart(
                fileName = "",
                onStopped = {
                    logSendRepository.sendLog("end", "センシングを終了しました。", 1)
                    Log.d("SettingViewModel", "センシングが停止しました")
                },
                sensingSecond = 30,
            )
        }
        return Result.success()
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
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // 通知の内容を設定
        val notificationBuilder =
            NotificationCompat.Builder(context, notificationChannelId)
                .setContentTitle("Sensing Service")
                .setContentText("Service is running...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        return notificationBuilder.build()
    }
}
