package net.kajilab.elpissender.api

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.util.Log
import pub.devrel.easypermissions.EasyPermissions

class BLEApi {
    // パーミッション確認用のコード
    private val permissionRequestCode = 1

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner

    private var leScanCallback: ScanCallback? = null
    private val tag = "BLEApi"

    private val permissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
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

    @SuppressLint("MissingPermission")
    fun startBLEBeaconScan(
        context: Context,
        resultBeacon: (ScanResult) -> Unit,
    ) {
        // パーミッションが許可された時にIbeaconが動く

        leScanCallback =
            object : ScanCallback() {
                override fun onScanResult(
                    callbackType: Int,
                    result: ScanResult,
                ) {
                    resultBeacon(result)
                }

                override fun onBatchScanResults(results: MutableList<ScanResult>) {
                    for (result in results) {
                        resultBeacon(result)
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    Log.e(tag, "BLE scan failed: $errorCode")
                }
            }

        if (EasyPermissions.hasPermissions(context, *permissions)) {
            val scanSettings =
                ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .build()
            bluetoothLeScanner?.startScan(null, scanSettings, leScanCallback)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopBLEBeaconScan() {
        bluetoothLeScanner?.stopScan(leScanCallback)
    }
}
