package net.kajilab.elpissender.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import io.reactivex.rxjava3.core.Single
import net.kajilab.elpissender.api.BLEApi
import net.kajilab.elpissender.utils.DateUtils
import net.kajilab.elpissender.utils.extension.SensorExtension
import java.io.File
import java.util.Locale
import kotlin.experimental.and

class BLERepository(context: Context) : SensorBase(context) {
    override val sensorType: Int = SensorExtension.TYPE_BLEBEACON
    override val sensorName: String = "BLEBeacon"

    private val tag: String = "BLERepository"

    private val bleApi = BLEApi()

    fun getPermission(activity: Activity) {
        bleApi.getPermission(context, activity)
    }

    override suspend fun start(
        filename: String,
        samplingFrequency: Double,
    ) {
        super.start(filename, samplingFrequency)

        bleApi.startBLEBeaconScan(context) { beacons ->
            // ここにビーコンの情報を受け取る処理を書く
            val time = DateUtils.getTimeStamp()
            val rssi = beacons?.rssi
            val mac = beacons?.device?.address // 一旦macアドレスは送らない設定

            val bytes = beacons?.scanRecord?.bytes ?: return@startBLEBeaconScan
            if (bytes.size > 30) {
                // iBeacon の場合 6 byte 目から、 9 byte 目はこの値に固定されている。
                if ((bytes.get(5) == 0x4c.toByte()) && (bytes.get(6) == 0x00.toByte()) &&
                    (bytes.get(7) == 0x02.toByte()) && (bytes.get(8) == 0x15.toByte())
                ) {
                    val uuid = (
                        intToHex2Line(bytes.get(9) and 0xff.toByte()) +
                            intToHex2Line(bytes.get(10) and 0xff.toByte()) +
                            intToHex2Line(bytes.get(11) and 0xff.toByte()) +
                            intToHex2Line(bytes.get(12) and 0xff.toByte()) +
                            "-" +
                            intToHex2Line(bytes.get(13) and 0xff.toByte()) +
                            intToHex2Line(bytes.get(14) and 0xff.toByte()) +
                            "-" +
                            intToHex2Line(bytes.get(15) and 0xff.toByte()) +
                            intToHex2Line(bytes.get(16) and 0xff.toByte()) +
                            "-" +
                            intToHex2Line(bytes.get(17) and 0xff.toByte()) +
                            intToHex2Line(bytes.get(18) and 0xff.toByte()) +
                            "-" +
                            intToHex2Line(bytes.get(19) and 0xff.toByte()) +
                            intToHex2Line(bytes.get(20) and 0xff.toByte()) +
                            intToHex2Line(bytes.get(21) and 0xff.toByte()) +
                            intToHex2Line(bytes.get(22) and 0xff.toByte()) +
                            intToHex2Line(bytes.get(23) and 0xff.toByte()) +
                            intToHex2Line(bytes.get(24) and 0xff.toByte())
                    )
                    val major =
                        intToHex2Line(bytes.get(25) and 0xff.toByte()) + intToHex2Line(bytes.get(26) and 0xff.toByte())
                    val minor =
                        intToHex2Line(bytes.get(27) and 0xff.toByte()) + intToHex2Line(bytes.get(28) and 0xff.toByte())

                    // major minor が16進数なので10進数に変える
                    val majorInt = major.toInt(16)
                    val minorInt = minor.toInt(16)

                    Log.d(tag, "uuid: $uuid, major: $majorInt, minor: $minorInt")

                    val data = "$time , $uuid , $rssi"
                    addQueue(
                        sensorName = sensorName,
                        timeStamp = time,
                        data = data,
                    )
                }
            }
        }
    }

    // intデータを 2桁16進数に変換するメソッド
    private fun intToHex2Line(i: Byte): String {
        val i2 = i.toInt()

        val hex2line =
            charArrayOf(
                Character.forDigit((i2 shr 4) and 0x0f, 16),
                Character.forDigit(i2 and 0x0f, 16),
            )
        val hex2str = String(hex2line)
        return hex2str.uppercase(Locale.getDefault())
    }

    override fun stop(): Single<File> {
        bleApi.stopBLEBeaconScan()

        return super.stop()
    }
}
