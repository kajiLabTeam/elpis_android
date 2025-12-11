@file:Suppress("ktlint")

// package net.kajilab.elpissender.utils

// import android.content.Context
// import com.google.firebase.analytics.FirebaseAnalytics

// class FirebaseLogger(context: Context) {
//
//    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
//
//    /**
//     * 任意のイベントを記録します。
//     *
//     * @param eventName イベント名（例: "custom_event"）
//     * @param params イベントの追加データ（例: mapOf("key" to "value")）
//     */
//    fun logEvent(eventName: String, params: Map<String, String> = emptyMap()) {
//        val bundle = android.os.Bundle()
//        for ((key, value) in params) {
//            bundle.putString(key, value)
//        }
//        firebaseAnalytics.logEvent(eventName, bundle)
//    }
//
//    /**
//     * デバッグ用に任意のメッセージを記録します。
//     *
//     * @param message ログメッセージ
//     */
//    fun logMessage(message: String) {
//        val date = java.util.Date()
//        val dateString = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss", java.util.Locale.getDefault()).format(date)
//        logEvent("custom_log", mapOf(
//            "message" to message,
//            "date" to dateString
//        ))
//    }
//
//    fun logSensing(
//        type: String,
//        message: String
//    ){
//        val date = java.util.Date()
//        val dateString = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss", java.util.Locale.getDefault()).format(date)
//        logEvent("sensing", mapOf(
//            "sensing_type" to type,
//            "message" to message,
//            "date" to dateString
//        ))
//    }
//
//    fun logSend(
//        result: String,
//        message: String
//    ){
//        val date = java.util.Date()
//        val dateString = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss", java.util.Locale.getDefault()).format(date)
//        logEvent("send_result", mapOf(
//            "result" to result,
//            "message" to message,
//            "date" to dateString
//        ))
//    }
// }
