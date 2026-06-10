package com.example.xbjsb.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.aiDataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_preferences")

class AiPreferences(private val context: Context) {
    companion object {
        private val PROVIDER_KEY = stringPreferencesKey("ai_provider")
        private val API_KEY = stringPreferencesKey("ai_api_key")
        private val BASE_URL_KEY = stringPreferencesKey("ai_base_url")
        private val MODEL_KEY = stringPreferencesKey("ai_model")

        const val OPENAI_BASE_URL = "https://api.openai.com/v1"
        const val OPENAI_MODEL = "gpt-4o-mini"
        const val DEEPSEEK_BASE_URL = "https://api.deepseek.com/v1"
        const val DEEPSEEK_MODEL = "deepseek-chat"
    }

    enum class AiProvider {
        OPENAI_COMPATIBLE,
        DEEPSEEK
    }

    data class AiConfig(
        val provider: AiProvider = AiProvider.DEEPSEEK,
        val apiKey: String = "",
        val baseUrl: String = DEEPSEEK_BASE_URL,
        val model: String = DEEPSEEK_MODEL
    ) {
        val isConfigured: Boolean get() = apiKey.isNotBlank() && baseUrl.isNotBlank() && model.isNotBlank()
    }

    val configFlow: Flow<AiConfig> = context.aiDataStore.data.map { preferences ->
        val provider = runCatching {
            AiProvider.valueOf(preferences[PROVIDER_KEY] ?: AiProvider.DEEPSEEK.name)
        }.getOrDefault(AiProvider.DEEPSEEK)

        val defaultBaseUrl = when (provider) {
            AiProvider.OPENAI_COMPATIBLE -> OPENAI_BASE_URL
            AiProvider.DEEPSEEK -> DEEPSEEK_BASE_URL
        }
        val defaultModel = when (provider) {
            AiProvider.OPENAI_COMPATIBLE -> OPENAI_MODEL
            AiProvider.DEEPSEEK -> DEEPSEEK_MODEL
        }

        AiConfig(
            provider = provider,
            apiKey = preferences[API_KEY] ?: "",
            baseUrl = preferences[BASE_URL_KEY] ?: defaultBaseUrl,
            model = preferences[MODEL_KEY] ?: defaultModel
        )
    }

    suspend fun setProvider(provider: AiProvider) {
        context.aiDataStore.edit { preferences ->
            preferences[PROVIDER_KEY] = provider.name
            when (provider) {
                AiProvider.OPENAI_COMPATIBLE -> {
                    preferences[BASE_URL_KEY] = OPENAI_BASE_URL
                    preferences[MODEL_KEY] = OPENAI_MODEL
                }
                AiProvider.DEEPSEEK -> {
                    preferences[BASE_URL_KEY] = DEEPSEEK_BASE_URL
                    preferences[MODEL_KEY] = DEEPSEEK_MODEL
                }
            }
        }
    }

    suspend fun saveConfig(
        provider: AiProvider,
        apiKey: String,
        baseUrl: String,
        model: String
    ) {
        context.aiDataStore.edit { preferences ->
            preferences[PROVIDER_KEY] = provider.name
            preferences[API_KEY] = apiKey.trim()
            preferences[BASE_URL_KEY] = baseUrl.trim().trimEnd('/')
            preferences[MODEL_KEY] = model.trim()
        }
    }
}
