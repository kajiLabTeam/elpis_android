package net.kajilab.elpissender.repository

import net.kajilab.elpissender.api.graylog.GraylogApiResponse
import net.kajilab.elpissender.entity.LogData

class LogSendRepository {
    val glaylogApiResponse = GraylogApiResponse()

    fun sendLog(
        result: String,
        message: String,
        level: Int,
    ) {
        // 端末名の取得
        val host = android.os.Build.MODEL
        val version = android.os.Build.VERSION.RELEASE

        val logData =
            LogData(
                result = result,
                shortMessage = message,
                level = level,
                version = version,
                host = host,
            )
        glaylogApiResponse.sendLog(logData)
    }
}
