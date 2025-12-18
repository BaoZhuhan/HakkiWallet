package com.example.account.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.account.BuildConfig

class ApiKeyStore(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "secure_prefs"
        private const val KEY_API = "ai_api_key"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        // Prefer EncryptedSharedPreferences; fall back to regular prefs if unavailable
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // If encryption APIs are not available for some reason, fall back to regular SharedPreferences
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    init {
        // If build config provides an API key at build time, persist it securely (only if not set)
        try {
            val buildKey = try { BuildConfig.AI_API_KEY } catch (_: Exception) { "" }
            if (!buildKey.isNullOrBlank() && sharedPreferences.getString(KEY_API, null).isNullOrBlank()) {
                setApiKey(buildKey)
            }
        } catch (_: Exception) {
            // BuildConfig may not be available; ignore
        }
    }

    fun setApiKey(key: String) {
        sharedPreferences.edit().putString(KEY_API, key).apply()
    }

    fun getApiKey(): String? {
        return sharedPreferences.getString(KEY_API, null)
    }

    fun clearApiKey() {
        sharedPreferences.edit().remove(KEY_API).apply()
    }
}
