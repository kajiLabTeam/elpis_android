package net.kajilab.elpissender.usecase

import android.app.Activity
import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kajilab.elpissender.API.SearedPreferenceApi
import net.kajilab.elpissender.API.http.ApiResponse
import net.kajilab.elpissender.R
import net.kajilab.elpissender.Repository.BLERepository
import net.kajilab.elpissender.Repository.SensingRepository
import net.kajilab.elpissender.Repository.SensorBase
import net.kajilab.elpissender.Repository.UserRepository
import net.kajilab.elpissender.Repository.WiFiRepository
import net.kajilab.elpissender.entity.User
import java.io.File

class SensingUsecase(
    private val context: Context
) {
    private val apiResponse = ApiResponse(context)
    private val sensorRepository = SensingRepository(context)
    private val searedPreferenceApi = SearedPreferenceApi()
    private val userRepository = UserRepository()

    private var scanFlag = false
    private var targetSensors: MutableList<SensorBase> = mutableListOf()

    // CoroutineScopeの定義
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    suspend fun firstStart(){
        val sensingTime = searedPreferenceApi.getIntegerValueByKey(
            key = "sensingTime",
            context = context
        )
        val waitTime = searedPreferenceApi.getIntegerValueByKey(
            key = "waitTime",
            context = context
        )
        scanFlag = true
        timerStart(
            fileName = "background",
            onStopped = {
                Log.d("SensingService","BackGroundで一回実行されたよ")
            },
            sensingTime = sensingTime,
            waitTime = waitTime
        )
    }

    fun finalStop() {
        scanFlag = false
        if (targetSensors.isNotEmpty()) {
            stop(
                onStopped = {
                    Log.d("SensingService","BackGroundで一回実行されたよ")
                }
            )
        }
    }

    fun start(fileName:String){
        addSensor(context = context)

        val samplingFrequency = -1.0
        coroutineScope.launch {
            sensorRepository.sensorStart(
                fileName = fileName,
                sensors = targetSensors,
                samplingFrequency = samplingFrequency
            )
        }
    }

    fun stop(
        onStopped:() -> Unit,
        onSend:((List<File?>)->Unit)? = null
    ){

        sensorRepository.sensorStop(
            sensors = targetSensors,
            onStopped = { sensorFileList ->
                val bleFile = sensorFileList[0]
                val wifiFile = sensorFileList[1]

                if (onSend != null) {
                    onSend( listOf( bleFile, wifiFile ) )
                }else{
                    val user = userRepository.getUserSetting(context)
                    if(
                        bleFile != null &&
                        wifiFile != null &&
                        user.userName != "" &&
                        user.password != "" &&
                        user.serverUrl != ""
                    ){
                        apiResponse.postCsvData(
                            wifiFile,
                            bleFile,
                            user.userName,
                            user.password,
                            user.serverUrl
                        )
                    }
                }

                onStopped()
            }
        )
        targetSensors = mutableListOf() // センサーをリセット
    }

    suspend fun timerStart(
        fileName:String,
        onStopped:() -> Unit,
        sensingTime:Int,
        onSend: ((List<File?>) -> Unit)? = null
    ){
        start(fileName)
        Log.d("Timer", "タイマー開始")
        delay(sensingTime * 1000L)
        Log.d("Timer", "タイマー終了")
        stop(
            onStopped = onStopped,
            onSend = onSend
        )
    }

    suspend private fun timerStart(
        fileName:String,
        onStopped:() -> Unit,
        sensingTime:Int,
        waitTime:Int
    ){
        while(scanFlag){
            start(fileName)
            Log.d("Timer", "タイマー開始")
            delay(sensingTime * 60 * 1000L)
            Log.d("Timer", "タイマー終了")
            if(targetSensors.isNotEmpty()){
                stop(onStopped)
                onStopped()
            }
            delay(waitTime * 60 * 1000L)
        }
    }

    private fun addSensor(context: Context){
        targetSensors.add(BLERepository(context))
        targetSensors.add(WiFiRepository(context))
    }
}