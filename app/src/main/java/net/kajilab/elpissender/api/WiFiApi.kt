package net.kajilab.elpissender.api

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.EasyPermissions

class WiFiApi {
    private val tag: String = "WiFiApi"

    // パーミッション確認用のコード
    private val permissionRequestCode = 2

    private var wifiScanReceiver: BroadcastReceiver? = null

    private val permissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        }

    fun getPermission(
        context: Context,
        activity: Activity,
    ) {
        // パーミッション確認
        if (!EasyPermissions.hasPermissions(context, *permissions)) {
            // パーミッションが許可されていない時の処理
            EasyPermissions.requestPermissions(activity, "パーミッションに関する説明", permissionRequestCode, *permissions)
        }
    }

    suspend fun scanWiFi(context: Context) {
        withContext(Dispatchers.IO) {
            // WiFiスキャンの非同期処理を実装
            // 例えば、WiFiManagerを使用してスキャンを実行する
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val success = wifiManager.startScan()
            if (!success) {
                Log.w(tag, "WiFiスキャンの開始に失敗しました")
            }
        }
    }

    fun getScanResults(
        context: Context,
        resultFunction: (MutableList<ScanResult>) -> Unit,
    ) {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

        wifiScanReceiver =
            object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context,
                    intent: Intent,
                ) {
                    val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
//                Log.d(TAG + "_WiFi","なんか受信はしたっぽい")
//                Log.d(TAG + "_WiFi",success.toString())
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val results = wifiManager.scanResults
                        resultFunction(results)
                    }
                }
            }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)
    }

    fun stop(context: Context) {
        try {
            context.unregisterReceiver(wifiScanReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiverが登録されていない場合の例外を無視
            Log.e(tag, "Receiver is not registered", e)
        }
    }
}
