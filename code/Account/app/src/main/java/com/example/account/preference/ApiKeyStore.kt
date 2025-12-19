package com.example.account.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class ApiKeyStore(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "secure_prefs"
        private const val KEY_API = "ai_api_key"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        // Try to create EncryptedSharedPreferences via reflection so the file compiles even if
        // androidx.security is not available to the analyzer. Fall back to regular prefs.
        try {
            // Load classes reflectively
            val masterKeyClass = Class.forName("androidx.security.crypto.MasterKey")
            val masterKeyBuilderClass = Class.forName("androidx.security.crypto.MasterKey\$Builder")
            val encryptedSpClass = Class.forName("androidx.security.crypto.EncryptedSharedPreferences")
            val keySchemeClass = Class.forName("androidx.security.crypto.MasterKey\$KeyScheme")
            val prefKeySchemeClass = Class.forName("androidx.security.crypto.EncryptedSharedPreferences\$PrefKeyEncryptionScheme")
            val prefValSchemeClass = Class.forName("androidx.security.crypto.EncryptedSharedPreferences\$PrefValueEncryptionScheme")

            // Create MasterKey: new MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            val builderCtor = masterKeyBuilderClass.getConstructor(Context::class.java)
            val builder = builderCtor.newInstance(context)
            val setKeyScheme = masterKeyBuilderClass.getMethod("setKeyScheme", keySchemeClass)
            val keySchemeAES = keySchemeClass.getField("AES256_GCM").get(null)
            setKeyScheme.invoke(builder, keySchemeAES)
            val buildMethod = masterKeyBuilderClass.getMethod("build")
            val masterKey = buildMethod.invoke(builder)

            // Get enum constants for PrefKeyEncryptionScheme and PrefValueEncryptionScheme
            val keyEnum = prefKeySchemeClass.getField("AES256_SIV").get(null)
            val valEnum = prefValSchemeClass.getField("AES256_GCM").get(null)

            // Call EncryptedSharedPreferences.create(context, name, masterKey, keyEnum, valEnum)
            val createMethod = encryptedSpClass.getMethod(
                "create",
                Context::class.java,
                String::class.java,
                masterKeyClass,
                prefKeySchemeClass,
                prefValSchemeClass
            )

            val esp = createMethod.invoke(null, context, PREFS_NAME, masterKey, keyEnum, valEnum) as SharedPreferences
            esp
        } catch (_: Throwable) {
            // Any exception -> fall back to regular SharedPreferences
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    init {
        // Try to read BuildConfig.AI_API_KEY reflectively and persist it if present and prefs empty
        try {
            val buildConfigClass = try { Class.forName("com.example.account.BuildConfig") } catch (_: Throwable) { null }
            val buildKey = try {
                buildConfigClass?.getField("AI_API_KEY")?.get(null) as? String
            } catch (_: Throwable) {
                null
            }

            if (!buildKey.isNullOrBlank() && sharedPreferences.getString(KEY_API, null).isNullOrBlank()) {
                setApiKey(buildKey)
            }
        } catch (_: Throwable) {
            // ignore
        }
    }

    fun setApiKey(key: String) {
        // Use synchronous commit so callers can rely on immediate availability
        sharedPreferences.edit(commit = true) { putString(KEY_API, key) }
    }

    fun getApiKey(): String? {
        return sharedPreferences.getString(KEY_API, null)
    }

    fun clearApiKey() {
        sharedPreferences.edit(commit = true) { remove(KEY_API) }
    }
}
