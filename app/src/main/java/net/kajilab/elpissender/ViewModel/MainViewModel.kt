package net.kajilab.elpissender.ViewModel

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.kajilab.elpissender.API.http.ApiResponse
import net.kajilab.elpissender.API.FileExplorerApi
import net.kajilab.elpissender.API.NotificationApi
import net.kajilab.elpissender.Repository.BLERepository
import net.kajilab.elpissender.Repository.SensingRepository
import net.kajilab.elpissender.Repository.SensorBase
import net.kajilab.elpissender.Repository.WiFiRepository
import java.io.File

data class PermissionState(
    val locationPermission: Boolean = false,
    val bluetoothPermission: Boolean = false,
    val wifiPermission: Boolean = false,
    val notificationPermission: Boolean = false,
    val backgroundLocationPermission: Boolean = false
)

sealed class UploadState {
    object Idle : UploadState()
    object Uploading : UploadState()
    data class Success(val message: String) : UploadState()
    data class Error(val message: String) : UploadState()
}

class MainViewModel (application: Application): AndroidViewModel(application) {

    val TAG: String = "MainViewModel"

    var targetSensors: MutableList<SensorBase> = mutableListOf()
    private val context get() = getApplication<Application>().applicationContext

    val apiResponse = ApiResponse(context)

    val sensorRepository = SensingRepository(context)

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _permissionState = MutableStateFlow(PermissionState())
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    private val _fileList = MutableStateFlow<List<File>>(emptyList())
    val fileList: StateFlow<List<File>> = _fileList.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    private val _bucketName = MutableStateFlow("fingerprint")
    val bucketName: StateFlow<String> = _bucketName.asStateFlow()

    fun setBucketName(name: String) {
        _bucketName.value = name
    }

    var sensorStartFlag = false

    val fileExplorerApi = FileExplorerApi(context)
    val notificationApi = NotificationApi()

    fun clearUploadState() {
        _uploadState.value = UploadState.Idle
    }

    fun checkPermissions() {
        val locationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val bluetoothGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val wifiGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_WIFI_STATE
        ) == PackageManager.PERMISSION_GRANTED

        val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val backgroundLocationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        _permissionState.value = PermissionState(
            locationPermission = locationGranted,
            bluetoothPermission = bluetoothGranted,
            wifiPermission = wifiGranted,
            notificationPermission = notificationGranted,
            backgroundLocationPermission = backgroundLocationGranted
        )
    }

    fun refreshFileList() {
        _fileList.value = fileExplorerApi.scanFile()
    }

    fun addSensor(){
        val bleRepository = BLERepository(context)
        targetSensors.add(bleRepository)
        targetSensors.add(WiFiRepository(context))
    }

    fun getPermission(activity: Activity){
        sensorRepository.getPermission(activity)
        notificationApi.getPermission(context,activity)
    }

    fun start(fileName:String){
        val samplingFrequency = -1.0
        viewModelScope.launch {
            _isScanning.value = true
            sensorRepository.sensorStart(
                fileName = fileName,
                sensors = targetSensors,
                samplingFrequency = samplingFrequency
            )
            sensorStartFlag = true
        }
    }

    fun stop(onStopped:() -> Unit = {}, autoUpload: Boolean = false){
        sensorRepository.sensorStop(
            sensors = targetSensors,
            onStopped = { sensorFileList ->
                if (autoUpload) {
                    val bleFile = sensorFileList.getOrNull(0)
                    val wifiFile = sensorFileList.getOrNull(1)
                    if(bleFile != null && wifiFile != null){
                        apiResponse.postCsvData(wifiFile, bleFile)
                    }
                }
                _isScanning.value = false
                refreshFileList()
                onStopped()
            }
        )
        sensorStartFlag = false
        targetSensors = mutableListOf()
    }

    suspend fun timerStart(fileName:String,onStopped:() -> Unit){
        start(fileName)
        Log.d("Timer", "タイマー開始")
        delay(10000)
        Log.d("Timer", "タイマー終了")
        stop(onStopped)
        onStopped()
    }

    fun scanFile() : List<File>{
        val fileList = fileExplorerApi.scanFile()
        Log.d(TAG,fileList.toString())
        return fileList
    }

    fun postCsvData(wifiFile: File, bleFile: File) {
        _uploadState.value = UploadState.Uploading

        var wifiSuccess = false
        var bleSuccess = false
        var errorMessage = ""

        val bucket = _bucketName.value

        apiResponse.uploadFileToMinio(
            file = wifiFile,
            bucketName = bucket,
            onSuccess = {
                wifiSuccess = true
                checkUploadComplete(wifiSuccess, bleSuccess, errorMessage)
            },
            onError = { error ->
                errorMessage = "WiFi: $error"
                _uploadState.value = UploadState.Error(errorMessage)
            }
        )

        apiResponse.uploadFileToMinio(
            file = bleFile,
            bucketName = bucket,
            onSuccess = {
                bleSuccess = true
                checkUploadComplete(wifiSuccess, bleSuccess, errorMessage)
            },
            onError = { error ->
                errorMessage = "BLE: $error"
                _uploadState.value = UploadState.Error(errorMessage)
            }
        )
    }

    private fun checkUploadComplete(wifiSuccess: Boolean, bleSuccess: Boolean, errorMessage: String) {
        if (wifiSuccess && bleSuccess) {
            _uploadState.value = UploadState.Success("アップロード完了")
        } else if (errorMessage.isNotEmpty()) {
            _uploadState.value = UploadState.Error(errorMessage)
        }
    }
}