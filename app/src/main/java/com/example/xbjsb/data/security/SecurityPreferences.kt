package com.example.xbjsb.data.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

val Context.securityDataStore: DataStore<Preferences> by preferencesDataStore(name = "security_preferences")

data class SecurityConfig(
    val isEnabled: Boolean = false,
    val passwordHash: String = "",
    val useBiometric: Boolean = false,
    val lockTimeout: Long = 0
)

class SecurityPreferences(private val context: Context) {
    private val PASSWORD_HASH_KEY = stringPreferencesKey("password_hash")
    private val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")
    private val LOCK_TIMEOUT_KEY = longPreferencesKey("lock_timeout")
    
    val configFlow: Flow<SecurityConfig> = context.securityDataStore.data.map { preferences ->
        val passwordHash = preferences[PASSWORD_HASH_KEY] ?: ""
        SecurityConfig(
            isEnabled = passwordHash.isNotBlank(),
            passwordHash = passwordHash,
            useBiometric = preferences[BIOMETRIC_ENABLED_KEY] ?: false,
            lockTimeout = preferences[LOCK_TIMEOUT_KEY] ?: 0L
        )
    }
    
    suspend fun setPassword(password: String) {
        val hash = hashPassword(password)
        context.securityDataStore.edit { preferences ->
            preferences[PASSWORD_HASH_KEY] = hash
        }
    }
    
    suspend fun verifyPassword(password: String): Boolean {
        val hash = hashPassword(password)
        val config = configFlow.first()
        return config.passwordHash == hash
    }
    
    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.securityDataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED_KEY] = enabled
        }
    }
    
    suspend fun setLockTimeout(timeoutMs: Long) {
        context.securityDataStore.edit { preferences ->
            preferences[LOCK_TIMEOUT_KEY] = timeoutMs
        }
    }
    
    suspend fun clearSecurity() {
        context.securityDataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}