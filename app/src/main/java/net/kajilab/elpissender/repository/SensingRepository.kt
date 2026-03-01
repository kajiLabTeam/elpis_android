package net.kajilab.elpissender.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import java.io.File

class SensingRepository(context: Context) {
    val tag: String = "SensingRepository"

    private val wifiRepository = WiFiRepository(context)
    private val bleRepository = BLERepository(context)

    fun getPermission(activity: Activity) {
        wifiRepository.getPermission(activity)
        bleRepository.getPermission(activity)
    }

    suspend fun sensorStart(
        fileName: String,
        sensors: MutableList<SensorBase>,
        samplingFrequency: Double,
    ) {
        require(fileName.isNotBlank()) { "File name cannot be empty" }
        require(samplingFrequency == -1.0 || samplingFrequency > 0) {
            "Sampling frequency must be positive or -1.0"
        }
        require(sensors.isNotEmpty()) { "Sensors list cannot be empty" }

        for (sensor in sensors) {
            try {
                sensor.init()
                sensor.start(
                    filename = fileName,
                    samplingFrequency = samplingFrequency,
                )
                Log.d(tag, "fileName = $fileName")
            } catch (e: Exception) {
                Log.e(tag, "センサー開始失敗", e)
                throw e
            }
        }
    }

    fun sensorStop(
        sensors: MutableList<SensorBase>,
        onStopped: (List<File?>) -> Unit,
    ) {
        val files =
            sensors.map { sensor ->
                try {
                    sensor.stop().blockingGet()
                } catch (e: Exception) {
                    Log.e(tag, "センサー停止 失敗", e)
                    null
                }
            }
        Log.d(tag, "センサー停止 完了")
        onStopped(files)
    }
}
