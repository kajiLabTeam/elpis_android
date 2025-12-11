package net.kajilab.elpissender.api.http

import android.content.Context
import android.util.Log
import net.kajilab.elpissender.repository.LogSendRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class ApiResponse(val context: Context) {
    private val logSendRepository = LogSendRepository()
    private val tag = "ApiResponse"

    // OkHttpClientのセットアップ
    private fun getOkHttpClient(
        username: String? = null,
        password: String? = null,
    ): OkHttpClient {
        val logging =
            HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            }

        val builder = OkHttpClient.Builder().addInterceptor(logging)
        username?.let {
            builder.addInterceptor(BasicAuthInterceptor(username, password ?: ""))
        }

        return builder.build()
    }

    // Retrofitのセットアップ
    private fun getRetrofit(
        url: String,
        client: OkHttpClient,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 共通のレスポンス処理
    private fun handleResponse(response: Response<ResponseBody>) {
        val responseString = response.body()?.string() ?: "null"
        logSendRepository.sendLog(response.code().toString(), responseString, 1)
        Log.d(tag, "ResponseCode: ${response.code()} , ResponseMessage: $responseString")

        if (response.isSuccessful) {
            Log.d(tag, "Response: $responseString")
        } else {
            Log.e(tag, "Request failed: ${response.message()}")
        }
    }

    // 共通のエラーハンドリング
    private fun handleFailure(t: Throwable) {
        val errorMessage =
            when (t) {
                is java.net.UnknownHostException -> "サーバーに接続できません"
                is java.net.SocketTimeoutException -> "接続がタイムアウトしました"
                else -> t.message ?: "不明なエラーが発生しました"
            }
        Log.e(tag, errorMessage, t)
        logSendRepository.sendLog("error", errorMessage, 2)
    }

    // モデルデータの送信
    fun postModelData(
        wifiFile: File,
        bleFile: File,
        roomId: Int,
        sampleType: String,
        url: String,
    ) {
        val client = getOkHttpClient()
        val retrofit = getRetrofit(url, client)
        val apiService = retrofit.create(ApiService::class.java)

        val wifiRequestBody = wifiFile.asRequestBody("text/csv".toMediaTypeOrNull())
        val wifiPart = MultipartBody.Part.createFormData("wifi_data", wifiFile.name, wifiRequestBody)

        val bleRequestBody = bleFile.asRequestBody("text/csv".toMediaTypeOrNull())
        val blePart = MultipartBody.Part.createFormData("ble_data", bleFile.name, bleRequestBody)

        val sampleTypePart = MultipartBody.Part.createFormData("sample_type", sampleType)
        val roomIdPart = MultipartBody.Part.createFormData("room_id", roomId.toString())

        val call = apiService.sendFingerPrintModel(wifiPart, blePart, sampleTypePart, roomIdPart)
        call.enqueue(
            object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>,
                ) {
                    handleResponse(response)
                }

                override fun onFailure(
                    call: Call<ResponseBody>,
                    t: Throwable,
                ) {
                    handleFailure(t)
                }
            },
        )
    }

    // CSVデータの送信
    fun postCsvData(
        wifiFile: File,
        bleFile: File,
        username: String,
        password: String,
        url: String,
    ) {
        val client = getOkHttpClient(username, password)
        val retrofit = getRetrofit(url, client)
        val apiService = retrofit.create(ApiService::class.java)

        val wifiRequestBody = wifiFile.asRequestBody("text/csv".toMediaTypeOrNull())
        val wifiPart = MultipartBody.Part.createFormData("wifi_data", wifiFile.name, wifiRequestBody)

        val bleRequestBody = bleFile.asRequestBody("text/csv".toMediaTypeOrNull())
        val blePart = MultipartBody.Part.createFormData("ble_data", bleFile.name, bleRequestBody)

        val call = apiService.submitSignalData(wifiPart, blePart)
        call.enqueue(
            object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>,
                ) {
                    handleResponse(response)
                }

                override fun onFailure(
                    call: Call<ResponseBody>,
                    t: Throwable,
                ) {
                    handleFailure(t)
                }
            },
        )
    }

    override fun toString(): String {
        return "ApiResponse()"
    }
}
