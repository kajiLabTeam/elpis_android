package net.kajilab.elpissender.API.http

import android.content.Context
import android.util.Log
import net.kajilab.elpissender.R
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApiResponse(val context: Context) {

    companion object {
        private const val TAG = "ApiResponse"
        private const val MINIO_USERNAME = "kajilab"
        private const val MINIO_PASSWORD = "fN4#Xfh4nNa\$3T@mhPlv"
        private const val BUCKET_NAME = "test"
    }

    private val apiService: ApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(BasicAuthInterceptor(MINIO_USERNAME, MINIO_PASSWORD))
            .build()

        Retrofit.Builder()
            .baseUrl(context.getString(R.string.base_url))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun postCsvData(wifiFile: File, bleFile: File, bucketName: String = BUCKET_NAME) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val path = "${dateFormat.format(Date())}/"

        uploadFileToMinio(file = wifiFile, bucketName = bucketName, path = path)
        uploadFileToMinio(file = bleFile, bucketName = bucketName, path = path)
    }

    fun uploadFileToMinio(
        file: File,
        bucketName: String = BUCKET_NAME,
        path: String = "",
        onSuccess: ((MinioUploadResponse) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val datePath = if (path.isEmpty()) "${dateFormat.format(Date())}/" else path

        // 手動でMultipartBodyを構築
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("bucket", bucketName)
            .addFormDataPart("path", datePath)
            .addFormDataPart("file", file.name, file.asRequestBody("application/octet-stream".toMediaTypeOrNull()))
            .build()

        Log.d(TAG, "Uploading file: ${file.name} to bucket: $bucketName, path: $datePath")

        val call = apiService.uploadFile(requestBody)
        call.enqueue(object : Callback<MinioUploadResponse> {
            override fun onResponse(
                call: Call<MinioUploadResponse>,
                response: Response<MinioUploadResponse>
            ) {
                if (response.isSuccessful) {
                    val uploadResponse = response.body()
                    Log.d(TAG, "Upload success: bucket=${uploadResponse?.bucket}, file=${uploadResponse?.file}, path=${uploadResponse?.path}")
                    onSuccess?.invoke(uploadResponse!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    val errorMsg = "Upload failed: ${response.code()} - $errorBody"
                    Log.e(TAG, errorMsg)
                    onError?.invoke(errorMsg)
                }
            }

            override fun onFailure(call: Call<MinioUploadResponse>, t: Throwable) {
                val errorMsg = "Upload error: ${t.message}"
                Log.e(TAG, errorMsg, t)
                onError?.invoke(errorMsg)
            }
        })
    }

    override fun toString(): String {
        return "ApiResponse()"
    }
}
