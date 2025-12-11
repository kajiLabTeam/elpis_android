package net.kajilab.elpissender.repository

import android.content.Context
import net.kajilab.elpissender.api.SharedPreferenceApi
import net.kajilab.elpissender.entity.User

class UserRepository {
    val sharedPreferenceApi = SharedPreferenceApi()

    companion object {
        private const val KEY_USER_NAME = "userName"
        private const val KEY_PASSWORD = "password"
        private const val KEY_SERVER_URL = "serverUrl"
    }

    fun getUserName(context: Context): String {
        return sharedPreferenceApi.getStringValueByKey(KEY_USER_NAME, context)
    }

    fun getPassword(context: Context): String {
        return sharedPreferenceApi.getSecureStringValueByKey(KEY_PASSWORD, context)
    }

    fun getServerUrl(context: Context): String {
        return sharedPreferenceApi.getStringValueByKey(KEY_SERVER_URL, context)
    }

    fun updateUserName(
        name: String,
        context: Context,
    ) {
        sharedPreferenceApi.setStringValueByKey(KEY_USER_NAME, name, context)
    }

    fun updatePassword(
        password: String,
        context: Context,
    ) {
        sharedPreferenceApi.setSecureStringValueByKey(KEY_PASSWORD, password, context)
    }

    fun updateServerUrl(
        url: String,
        context: Context,
    ) {
        sharedPreferenceApi.setStringValueByKey(KEY_SERVER_URL, url, context)
    }

    fun saveUserSetting(
        userName: String,
        password: String,
        serverUrl: String,
        context: Context,
    ) {
        updateUserName(userName, context)
        updatePassword(password, context)
        updateServerUrl(serverUrl, context)
    }

    fun getUserSetting(context: Context): User {
        val userName = getUserName(context)
        val password = getPassword(context)
        val serverUrl = getServerUrl(context)
        return User(
            userName,
            password,
            serverUrl,
        )
    }
}
