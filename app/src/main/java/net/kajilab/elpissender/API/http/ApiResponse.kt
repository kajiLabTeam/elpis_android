package net.kajilab.elpissender.API.http

import android.content.Context
import com.google.gson.Gson
import net.kajilab.elpissender.R
import net.kajilab.elpissender.entity.FingerPrintSendData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class ApiResponse(val context: Context) {
    fun postModelData(
        wifiFile: File,
        bleFile:File,
        roomId:Int,
        sampleType:String,
        url: String
    ){
        // OkHttpClientのセットアップ
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        // Retrofitのセットアップ
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        // ファイルをリクエストボディに変換
        val wifiRequestBody = wifiFile.asRequestBody("text/csv".toMediaTypeOrNull())
        val wifiPart = MultipartBody.Part.createFormData("wifi_data", wifiFile.name, wifiRequestBody)

        val bleRequestBody = bleFile.asRequestBody("text/csv".toMediaTypeOrNull())
        val blePart = MultipartBody.Part.createFormData("ble_data", bleFile.name, bleRequestBody)

        val request = FingerPrintSendData(
            sampleType = sampleType,
            roomId = roomId
        )

        val gson = Gson()
        val requestJson = gson.toJson(request)
        val requestBody = requestJson.toRequestBody("application/json".toMediaTypeOrNull())

        // リクエストを送信
        val call = apiService.sendFingerPrintModel(wifiPart, blePart, requestBody)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // 成功時の処理
                    val responseString = response.body()?.string()
                    println("Response: $responseString")
                } else {
                    // 失敗時の処理
                    println("Request failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 通信エラー時の処理
                t.printStackTrace()
            }
        })
    }


    fun postCsvData(
        wifiFile: File,
        bleFile:File,
        username: String,
        password: String,
        url: String
    ) {
        // OkHttpClientのセットアップ
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(BasicAuthInterceptor(username, password))
            .build()

        // Retrofitのセットアップ
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        // ファイルをリクエストボディに変換
        val wifiRequestBody = wifiFile.asRequestBody("text/csv".toMediaTypeOrNull())
        val wifiPart = MultipartBody.Part.createFormData("wifi_data", wifiFile.name, wifiRequestBody)

        val bleRequestBody = bleFile.asRequestBody("text/csv".toMediaTypeOrNull())
        val blePart = MultipartBody.Part.createFormData("ble_data", bleFile.name, bleRequestBody)

        // リクエストを送信
        val call = apiService.submitSignalData(wifiPart, blePart)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // 成功時の処理
                    val responseString = response.body()?.string()
                    println("Response: $responseString")
                } else {
                    // 失敗時の処理
                    println("Request failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 通信エラー時の処理
                t.printStackTrace()
            }
        })
    }

    override fun toString(): String {
        return "ApiResponse()"
    }
}