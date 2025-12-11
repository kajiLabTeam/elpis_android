package net.kajilab.elpissender.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import io.reactivex.rxjava3.core.Single
import net.kajilab.elpissender.api.OtherFileStorageApi
import java.io.File

abstract class SensorBase(val context: Context) : SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var preSensor: Sensor? = null
    private val queue: ArrayDeque<String> = ArrayDeque(listOf())
    private var otherFileStorage: OtherFileStorageApi? = null

    var samplingFrequency = -1.0

    abstract val sensorType: Int
    abstract val sensorName: String

    fun init() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        preSensor = sensorManager?.getDefaultSensor(sensorType)
    }

    open suspend fun start(
        filename: String,
        samplingFrequency: Double,
    ) {
        queue.clear()
        otherFileStorage = OtherFileStorageApi(context, "${filename}_$sensorName", queue)
        otherFileStorage?.saveAtBatch()
        sensorManager?.registerListener(this, preSensor, SensorManager.SENSOR_DELAY_UI)

        this.samplingFrequency = samplingFrequency
    }

    open fun stop(): Single<File> {
        val sensingFile = otherFileStorage?.stop()
        sensorManager?.unregisterListener(this)

        return if (sensingFile != null) {
            Single.just(sensingFile)
        } else {
            Single.error(Exception("sensingFile is null"))
        }
    }

    private var nowTime: Long = 0

    fun addQueue(
        sensorName: String,
        data: String,
        timeStamp: Long,
    ) {
        // -1.0の場合はサンプリング周波数を考慮しない
        if (samplingFrequency == -1.0) {
            queue.add(data)
            nowTime = timeStamp
            Log.d(sensorName, data)
            return
        }

        if (timeStamp - nowTime > frequency2second(samplingFrequency) * 1000) {
            queue.add(data)
            nowTime = timeStamp
            Log.d(sensorName, data)
        }
    }

    private fun frequency2second(frequency: Double): Double {
        return 1.0 / frequency
    }

    override fun onSensorChanged(event: SensorEvent) {
    }

    override fun onAccuracyChanged(
        p0: Sensor?,
        p1: Int,
    ) {
    }
}
