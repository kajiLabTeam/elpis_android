package net.kajilab.elpissender.API.http

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/object/upload")
    fun uploadFile(
        @Body body: MultipartBody
    ): Call<MinioUploadResponse>
}

data class MinioUploadResponse(
    val bucket: String,
    val file: String,
    val path: String
)
