package com.example.xbjsb.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.xbjsb.ui.theme.ThemeColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

class ThemePreferences(private val context: Context) {
    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val THEME_COLOR_KEY = stringPreferencesKey("theme_color")
        private val FROSTED_BLUR_ENABLED_KEY = booleanPreferencesKey("frosted_blur_enabled")
        private val ANIMATION_SPEED_KEY = stringPreferencesKey("animation_speed")
    }

    enum class ThemeMode {
        LIGHT,
        DARK,
        SYSTEM
    }

    enum class AnimationSpeed(val displayName: String) {
        ELEGANT("优雅"),
        STANDARD("标准"),
        SWIFT("迅速")
    }

    val themeModeFlow: Flow<ThemeMode> = context.themeDataStore.data
        .map { preferences ->
            val mode = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
            ThemeMode.valueOf(mode)
        }

    val themeColorFlow: Flow<ThemeColor> = context.themeDataStore.data
        .map { preferences ->
            val color = preferences[THEME_COLOR_KEY] ?: ThemeColor.ORANGE.name
            ThemeColor.valueOf(color)
        }

    val frostedBlurEnabledFlow: Flow<Boolean> = context.themeDataStore.data
        .map { preferences ->
            preferences[FROSTED_BLUR_ENABLED_KEY] ?: false
        }

    val animationSpeedFlow: Flow<AnimationSpeed> = context.themeDataStore.data
        .map { preferences ->
            val speed = preferences[ANIMATION_SPEED_KEY] ?: AnimationSpeed.ELEGANT.name
            AnimationSpeed.valueOf(speed)
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    suspend fun setThemeColor(color: ThemeColor) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_COLOR_KEY] = color.name
        }
    }

    suspend fun setFrostedBlurEnabled(enabled: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[FROSTED_BLUR_ENABLED_KEY] = enabled
        }
    }

    suspend fun setAnimationSpeed(speed: AnimationSpeed) {
        context.themeDataStore.edit { preferences ->
            preferences[ANIMATION_SPEED_KEY] = speed.name
        }
    }
}
