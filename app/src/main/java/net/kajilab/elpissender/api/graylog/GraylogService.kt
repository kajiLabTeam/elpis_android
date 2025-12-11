package net.kajilab.elpissender.api.graylog

import net.kajilab.elpissender.entity.LogData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GraylogService {
    @Headers("Content-Type: application/json")
    @POST("gelf")
    fun sendLog(
        @Body logData: LogData,
    ): Call<Void>
}
