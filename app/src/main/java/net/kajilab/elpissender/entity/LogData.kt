package net.kajilab.elpissender.entity

import com.google.gson.annotations.SerializedName

data class LogData(
    @SerializedName("version") val version: String,
    @SerializedName("host") val host: String,
    @SerializedName("result") val result: String,
    @SerializedName("short_message") val shortMessage: String,
    @SerializedName("level") val level: Int,
)
