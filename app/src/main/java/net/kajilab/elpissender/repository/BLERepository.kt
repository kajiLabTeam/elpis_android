package net.kajilab.elpissender.repository

import android.app.Activity
import android.content.Context
import io.reactivex.rxjava3.core.Single
import net.kajilab.elpissender.api.BLEApi
import net.kajilab.elpissender.utils.BleAdvertisementParser
import net.kajilab.elpissender.utils.DateUtils
import net.kajilab.elpissender.utils.extension.SensorExtension
import java.io.File

class BLERepository(context: Context) : SensorBase(context) {
    override val sensorType: Int = SensorExtension.TYPE_BLEBEACON
    override val sensorName: String = "BLEBeacon"

    private val bleApi = BLEApi()

    fun getPermission(activity: Activity) {
        bleApi.getPermission(context, activity)
    }

    override suspend fun start(
        filename: String,
        samplingFrequency: Double,
    ) {
        super.start(filename, samplingFrequency)

        bleApi.startBLEBeaconScan(context) { beacon ->
            val time = DateUtils.getTimeStamp()
            val rssi = beacon.rssi
            val bytes = beacon.scanRecord?.bytes ?: return@startBLEBeaconScan
            val uuids = BleAdvertisementParser.parseUuids(bytes)

            if (uuids.isEmpty()) {
                return@startBLEBeaconScan
            }

            for (uuid in uuids) {
                addQueue(
                    sensorName = sensorName,
                    timeStamp = time,
                    data = "$time , $uuid , $rssi",
                )
            }
        }
    }

    override fun stop(): Single<File> {
        bleApi.stopBLEBeaconScan()

        return super.stop()
    }
}
