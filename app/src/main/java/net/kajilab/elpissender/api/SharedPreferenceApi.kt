package net.kajilab.elpissender.api

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SharedPreferenceApi {
    private val preferenceFileKey = "SearedPreference"

    private lateinit var encryptedPrefs: SharedPreferences

    private fun initEncryptedPrefs(context: Context) {
        if (!::encryptedPrefs.isInitialized) {
            val masterKey =
                MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

            encryptedPrefs =
                EncryptedSharedPreferences.create(
                    context,
                    "encrypted_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                )
        }
    }

    fun getSecureStringValueByKey(
        key: String,
        context: Context,
    ): String {
        initEncryptedPrefs(context)
        return encryptedPrefs.getString(key, "") ?: ""
    }

    fun setSecureStringValueByKey(
        key: String,
        value: String,
        context: Context,
    ) {
        initEncryptedPrefs(context)
        with(encryptedPrefs.edit()) {
            putString(key, value)
            apply()
        }
    }

    fun getStringValueByKey(
        key: String,
        context: Context,
    ): String {
        val sharedPref =
            context.getSharedPreferences(
                preferenceFileKey,
                Context.MODE_PRIVATE,
            )

        return sharedPref.getString(key, "") ?: ""
    }

    fun setStringValueByKey(
        key: String,
        value: String,
        context: Context,
    ) {
        val sharedPref =
            context.getSharedPreferences(
                preferenceFileKey,
                Context.MODE_PRIVATE,
            )

        with(sharedPref.edit()) {
            putString(key, value)
            apply()
        }
    }

    fun getIntegerValueByKey(
        key: String,
        defaultValue: Int = 1,
        context: Context,
    ): Int {
        val sharedPref =
            context.getSharedPreferences(
                preferenceFileKey,
                Context.MODE_PRIVATE,
            )

        return sharedPref.getInt(key, defaultValue)
    }

    fun setIntegerValueByKey(
        key: String,
        value: Int,
        context: Context,
    ) {
        val sharedPref =
            context.getSharedPreferences(
                preferenceFileKey,
                Context.MODE_PRIVATE,
            )

        with(sharedPref.edit()) {
            putInt(key, value)
            apply()
        }
    }

    fun getBooleanValueByKey(
        key: String,
        context: Context,
    ): Boolean {
        val sharedPref =
            context.getSharedPreferences(
                preferenceFileKey,
                Context.MODE_PRIVATE,
            )

        return sharedPref.getBoolean(key, false)
    }

    fun setBooleanValueByKey(
        key: String,
        value: Boolean,
        context: Context,
    ) {
        val sharedPref =
            context.getSharedPreferences(
                preferenceFileKey,
                Context.MODE_PRIVATE,
            )

        with(sharedPref.edit()) {
            putBoolean(key, value)
            apply()
        }
    }
}
