package net.kajilab.elpissender.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
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
        require(samplingFrequency > 0) { "Sampling frequency must be positive" }
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

    private val compositeDisposable = CompositeDisposable()

    fun sensorStop(
        sensors: MutableList<SensorBase>,
        onStopped: (List<File?>) -> Unit,
    ) {
        val singles =
            sensors.map { sensor ->
                sensor.stop() // This should return Single<File?>
            }

        compositeDisposable.add(
            Single.zip(singles) { results ->
                results.map { it as File? }
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { files ->
                        Log.d(tag, "センサー停止 成功")
                        // センサーが終了した時にMainActivityに伝える。
                        onStopped(files)
                    },
                    { e ->
                        Log.e(tag, "センサー停止 失敗", e)
                    },
                ),
        )
    }

    fun onCleared() {
        compositeDisposable.clear()
    }
}
