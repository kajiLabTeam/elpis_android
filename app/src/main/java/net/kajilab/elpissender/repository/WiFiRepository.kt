package net.kajilab.elpissender.repository

import android.app.Activity
import android.content.Context
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.kajilab.elpissender.api.WiFiApi
import net.kajilab.elpissender.utils.DateUtils
import net.kajilab.elpissender.utils.extension.SensorExtension
import java.io.File

class WiFiRepository(context: Context) : SensorBase(context) {
    override val sensorType: Int = SensorExtension.TYPE_WIFI
    override val sensorName: String = "WiFi"

    @Volatile
    private var isScanning = false

    val wifiApi = WiFiApi()

    fun getPermission(activity: Activity) {
        wifiApi.getPermission(context, activity)
    }

    override suspend fun start(
        filename: String,
        samplingFrequency: Double,
    ) {
        super.start(filename, samplingFrequency)
        wifiApi.getScanResults(context) { scanResults ->
            val time = DateUtils.getTimeStamp()
            for (scanResult in scanResults) {
                val data = "$time , ${scanResult.BSSID} , ${scanResult.level}"
                addQueue(
                    sensorName = sensorName,
                    timeStamp = time,
                    data = data,
                )
            }
        }

        val scanInterval = 1000L
        isScanning = true

        // 以下の処理を止めるまで繰り返す
        withContext(Dispatchers.IO) {
            while (isScanning) {
                wifiApi.scanWiFi(context) // 非同期に処理を行う
                delay(scanInterval)
            }
        }
    }

    override fun stop(): Single<File> {
        wifiApi.stop(context)
        isScanning = false
        return super.stop()
    }
}
