package net.kajilab.elpissender.Repository

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import io.reactivex.rxjava3.core.Single
import net.kajilab.elpissender.API.BLEApi
import net.kajilab.elpissender.Utils.DateUtils
import net.kajilab.elpissender.Utils.extension.SensorExtension
import org.altbeacon.beacon.Beacon
import java.io.File


class BLERepository(context: Context): SensorBase(context) {

    override val sensorType: Int = SensorExtension.TYPE_BLEBEACON
    override val sensorName: String = "BLEBeacon"

    var lifecycleOwner: LifecycleOwner? = null

    val TAG: String = "BLERepository"

    val bleApi = BLEApi()

    fun getPermission(activity: Activity){
        bleApi.getPermission(context, activity)
    }

    override suspend fun start(filename: String, samplingFrequency: Double) {
        super.start(filename, samplingFrequency)

        if (lifecycleOwner != null) {
            bleApi.startBLEBeaconScan(context, lifecycleOwner!!){ beacons: Collection<Beacon> ->
                //ここにビーコンの情報を受け取る処理を書く
                val time = DateUtils.getTimeStamp()

                for(beacon in beacons){
                    val uuid = beacon.id1
                    val rssi = beacon.rssi

                    val data = "$time , $uuid , $rssi"
                    addQueue(
                        sensorName = sensorName,
                        timeStamp = time,
                        data = data
                    )
                }
            }
        }
    }

    override fun stop(): Single<File> {
        bleApi.stopBLEBeaconScan()

        return super.stop()
    }
}