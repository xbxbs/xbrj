package com.example.xbjsb.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ============================================
// 构建不同主题的 ColorScheme
// ============================================

private fun getLightColorScheme(themeColor: ThemeColor) = when (themeColor) {
    ThemeColor.ORANGE -> lightColorScheme(
        primary = OrangeTheme.primary,
        onPrimary = Color.White,
        primaryContainer = OrangeTheme.primaryContainer,
        onPrimaryContainer = Color(0xFF3E0B00),
        
        secondary = OrangeTheme.secondary,
        onSecondary = Color.White,
        secondaryContainer = OrangeTheme.secondaryContainer,
        onSecondaryContainer = Color(0xFF4E2600),
        
        tertiary = Color(0xFFFF8A50),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFDCC6),
        onTertiaryContainer = Color(0xFF2A1100),
        
        error = FunctionalColors.error,
        onError = FunctionalColors.onError,
        errorContainer = FunctionalColors.errorContainer,
        onErrorContainer = FunctionalColors.onErrorContainer,
        
        background = NeutralColors.backgroundLight,
        onBackground = NeutralColors.onSurfaceLight,
        
        surface = NeutralColors.surfaceLight,
        onSurface = NeutralColors.onSurfaceLight,
        surfaceVariant = NeutralColors.surfaceVariantLight,
        onSurfaceVariant = NeutralColors.onSurfaceVariantLight,
        
        outline = NeutralColors.outlineLight,
        outlineVariant = NeutralColors.outlineVariantLight
    )
    
    ThemeColor.BLUE -> lightColorScheme(
        primary = BlueTheme.primary,
        onPrimary = Color.White,
        primaryContainer = BlueTheme.primaryContainer,
        onPrimaryContainer = Color(0xFF001B3D),
        
        secondary = BlueTheme.secondary,
        onSecondary = Color.White,
        secondaryContainer = BlueTheme.secondaryContainer,
        onSecondaryContainer = Color(0xFF002106),
        
        tertiary = Color(0xFF0288D1),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFB3E5FC),
        onTertiaryContainer = Color(0xFF001A24),
        
        error = FunctionalColors.error,
        onError = FunctionalColors.onError,
        errorContainer = FunctionalColors.errorContainer,
        onErrorContainer = FunctionalColors.onErrorContainer,
        
        background = NeutralColors.backgroundLight,
        onBackground = NeutralColors.onSurfaceLight,
        
        surface = NeutralColors.surfaceLight,
        onSurface = NeutralColors.onSurfaceLight,
        surfaceVariant = NeutralColors.surfaceVariantLight,
        onSurfaceVariant = NeutralColors.onSurfaceVariantLight,
        
        outline = NeutralColors.outlineLight,
        outlineVariant = NeutralColors.outlineVariantLight
    )
    
    ThemeColor.PURPLE -> lightColorScheme(
        primary = PurpleTheme.primary,
        onPrimary = Color.White,
        primaryContainer = PurpleTheme.primaryContainer,
        onPrimaryContainer = Color(0xFF2E0051),
        
        secondary = PurpleTheme.secondary,
        onSecondary = Color.White,
        secondaryContainer = PurpleTheme.secondaryContainer,
        onSecondaryContainer = Color(0xFF22005D),
        
        tertiary = Color(0xFF7B1FA2),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFE1BEE7),
        onTertiaryContainer = Color(0xFF1A0033),
        
        error = FunctionalColors.error,
        onError = FunctionalColors.onError,
        errorContainer = FunctionalColors.errorContainer,
        onErrorContainer = FunctionalColors.onErrorContainer,
        
        background = NeutralColors.backgroundLight,
        onBackground = NeutralColors.onSurfaceLight,
        
        surface = NeutralColors.surfaceLight,
        onSurface = NeutralColors.onSurfaceLight,
        surfaceVariant = NeutralColors.surfaceVariantLight,
        onSurfaceVariant = NeutralColors.onSurfaceVariantLight,
        
        outline = NeutralColors.outlineLight,
        outlineVariant = NeutralColors.outlineVariantLight
    )
    
    ThemeColor.GREEN -> lightColorScheme(
        primary = GreenTheme.primary,
        onPrimary = Color.White,
        primaryContainer = GreenTheme.primaryContainer,
        onPrimaryContainer = Color(0xFF072100),
        
        secondary = GreenTheme.secondary,
        onSecondary = Color.White,
        secondaryContainer = GreenTheme.secondaryContainer,
        onSecondaryContainer = Color(0xFF0B2F00),
        
        tertiary = Color(0xFF388E3C),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFC8E6C9),
        onTertiaryContainer = Color(0xFF00210B),
        
        error = FunctionalColors.error,
        onError = FunctionalColors.onError,
        errorContainer = FunctionalColors.errorContainer,
        onErrorContainer = FunctionalColors.onErrorContainer,
        
        background = NeutralColors.backgroundLight,
        onBackground = NeutralColors.onSurfaceLight,
        
        surface = NeutralColors.surfaceLight,
        onSurface = NeutralColors.onSurfaceLight,
        surfaceVariant = NeutralColors.surfaceVariantLight,
        onSurfaceVariant = NeutralColors.onSurfaceVariantLight,
        
        outline = NeutralColors.outlineLight,
        outlineVariant = NeutralColors.outlineVariantLight
    )
    
    ThemeColor.PINK -> lightColorScheme(
        primary = PinkTheme.primary,
        onPrimary = Color.White,
        primaryContainer = PinkTheme.primaryContainer,
        onPrimaryContainer = Color(0xFF3E001D),
        
        secondary = PinkTheme.secondary,
        onSecondary = Color.White,
        secondaryContainer = PinkTheme.secondaryContainer,
        onSecondaryContainer = Color(0xFF4E1B00),
        
        tertiary = Color(0xFFC2185B),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFF8BBD0),
        onTertiaryContainer = Color(0xFF31001D),
        
        error = FunctionalColors.error,
        onError = FunctionalColors.onError,
        errorContainer = FunctionalColors.errorContainer,
        onErrorContainer = FunctionalColors.onErrorContainer,
        
        background = NeutralColors.backgroundLight,
        onBackground = NeutralColors.onSurfaceLight,
        
        surface = NeutralColors.surfaceLight,
        onSurface = NeutralColors.onSurfaceLight,
        surfaceVariant = NeutralColors.surfaceVariantLight,
        onSurfaceVariant = NeutralColors.onSurfaceVariantLight,
        
        outline = NeutralColors.outlineLight,
        outlineVariant = NeutralColors.outlineVariantLight
    )
}

private fun getDarkColorScheme(themeColor: ThemeColor) = when (themeColor) {
    ThemeColor.ORANGE -> darkColorScheme(
        primary = OrangeTheme.primaryDark,
        onPrimary = Color(0xFF5C2912),
        primaryContainer = OrangeTheme.primaryContainerDark,
        onPrimaryContainer = Color(0xFFFFDCC1),
        
        secondary = OrangeTheme.secondaryDark,
        onSecondary = Color(0xFF4E3619),
        secondaryContainer = OrangeTheme.secondaryContainerDark,
        onSecondaryContainer = Color(0xFFFFF4E6),
        
        tertiary = Color(0xFFFFCC99),
        onTertiary = Color(0xFF4E2600),
        tertiaryContainer = Color(0xFF6B3700),
        onTertiaryContainer = Color(0xFFFFEAD1),
        
        error = FunctionalColors.errorDark,
        onError = FunctionalColors.onErrorDark,
        errorContainer = FunctionalColors.errorContainerDark,
        onErrorContainer = FunctionalColors.onErrorContainerDark,
        
        background = NeutralColors.backgroundDark,
        onBackground = NeutralColors.onSurfaceDark,
        
        surface = NeutralColors.surfaceDark,
        onSurface = NeutralColors.onSurfaceDark,
        surfaceVariant = NeutralColors.surfaceVariantDark,
        onSurfaceVariant = NeutralColors.onSurfaceVariantDark,
        
        outline = NeutralColors.outlineDark,
        outlineVariant = NeutralColors.outlineVariantDark
    )
    
    ThemeColor.BLUE -> darkColorScheme(
        primary = BlueTheme.primaryDark,
        onPrimary = Color(0xFF003258),
        primaryContainer = BlueTheme.primaryContainerDark,
        onPrimaryContainer = Color(0xFFD1E4FF),
        
        secondary = BlueTheme.secondaryDark,
        onSecondary = Color(0xFF003A14),
        secondaryContainer = BlueTheme.secondaryContainerDark,
        onSecondaryContainer = Color(0xFFC3E8C0),
        
        tertiary = Color(0xFF81D4FA),
        onTertiary = Color(0xFF00344F),
        tertiaryContainer = Color(0xFF004C6E),
        onTertiaryContainer = Color(0xFFB3E5FC),
        
        error = FunctionalColors.errorDark,
        onError = FunctionalColors.onErrorDark,
        errorContainer = FunctionalColors.errorContainerDark,
        onErrorContainer = FunctionalColors.onErrorContainerDark,
        
        background = NeutralColors.backgroundDark,
        onBackground = NeutralColors.onSurfaceDark,
        
        surface = NeutralColors.surfaceDark,
        onSurface = NeutralColors.onSurfaceDark,
        surfaceVariant = NeutralColors.surfaceVariantDark,
        onSurfaceVariant = NeutralColors.onSurfaceVariantDark,
        
        outline = NeutralColors.outlineDark,
        outlineVariant = NeutralColors.outlineVariantDark
    )
    
    ThemeColor.PURPLE -> darkColorScheme(
        primary = PurpleTheme.primaryDark,
        onPrimary = Color(0xFF56008A),
        primaryContainer = PurpleTheme.primaryContainerDark,
        onPrimaryContainer = Color(0xFFF2DAFF),
        
        secondary = PurpleTheme.secondaryDark,
        onSecondary = Color(0xFF3B1A75),
        secondaryContainer = PurpleTheme.secondaryContainerDark,
        onSecondaryContainer = Color(0xFFEBDDFF),
        
        tertiary = Color(0xFFCE93D8),
        onTertiary = Color(0xFF4A006E),
        tertiaryContainer = Color(0xFF6B0095),
        onTertiaryContainer = Color(0xFFF2DAFF),
        
        error = FunctionalColors.errorDark,
        onError = FunctionalColors.onErrorDark,
        errorContainer = FunctionalColors.errorContainerDark,
        onErrorContainer = FunctionalColors.onErrorContainerDark,
        
        background = NeutralColors.backgroundDark,
        onBackground = NeutralColors.onSurfaceDark,
        
        surface = NeutralColors.surfaceDark,
        onSurface = NeutralColors.onSurfaceDark,
        surfaceVariant = NeutralColors.surfaceVariantDark,
        onSurfaceVariant = NeutralColors.onSurfaceVariantDark,
        
        outline = NeutralColors.outlineDark,
        outlineVariant = NeutralColors.outlineVariantDark
    )
    
    ThemeColor.GREEN -> darkColorScheme(
        primary = GreenTheme.primaryDark,
        onPrimary = Color(0xFF043900),
        primaryContainer = GreenTheme.primaryContainerDark,
        onPrimaryContainer = Color(0xFFA0D99D),
        
        secondary = GreenTheme.secondaryDark,
        onSecondary = Color(0xFF164800),
        secondaryContainer = GreenTheme.secondaryContainerDark,
        onSecondaryContainer = Color(0xFFD6EBB7),
        
        tertiary = Color(0xFFA5D6A7),
        onTertiary = Color(0xFF003911),
        tertiaryContainer = Color(0xFF00531D),
        onTertiaryContainer = Color(0xFFC8E6C9),
        
        error = FunctionalColors.errorDark,
        onError = FunctionalColors.onErrorDark,
        errorContainer = FunctionalColors.errorContainerDark,
        onErrorContainer = FunctionalColors.onErrorContainerDark,
        
        background = NeutralColors.backgroundDark,
        onBackground = NeutralColors.onSurfaceDark,
        
        surface = NeutralColors.surfaceDark,
        onSurface = NeutralColors.onSurfaceDark,
        surfaceVariant = NeutralColors.surfaceVariantDark,
        onSurfaceVariant = NeutralColors.onSurfaceVariantDark,
        
        outline = NeutralColors.outlineDark,
        outlineVariant = NeutralColors.outlineVariantDark
    )
    
    ThemeColor.PINK -> darkColorScheme(
        primary = PinkTheme.primaryDark,
        onPrimary = Color(0xFF650033),
        primaryContainer = PinkTheme.primaryContainerDark,
        onPrimaryContainer = Color(0xFFFFD9E2),
        
        secondary = PinkTheme.secondaryDark,
        onSecondary = Color(0xFF7D3100),
        secondaryContainer = PinkTheme.secondaryContainerDark,
        onSecondaryContainer = Color(0xFFFFDBCE),
        
        tertiary = Color(0xFFF48FB1),
        onTertiary = Color(0xFF5F0037),
        tertiaryContainer = Color(0xFF890050),
        onTertiaryContainer = Color(0xFFFFD9E2),
        
        error = FunctionalColors.errorDark,
        onError = FunctionalColors.onErrorDark,
        errorContainer = FunctionalColors.errorContainerDark,
        onErrorContainer = FunctionalColors.onErrorContainerDark,
        
        background = NeutralColors.backgroundDark,
        onBackground = NeutralColors.onSurfaceDark,
        
        surface = NeutralColors.surfaceDark,
        onSurface = NeutralColors.onSurfaceDark,
        surfaceVariant = NeutralColors.surfaceVariantDark,
        onSurfaceVariant = NeutralColors.onSurfaceVariantDark,
        
        outline = NeutralColors.outlineDark,
        outlineVariant = NeutralColors.outlineVariantDark
    )
}

// ============================================
// 主题 Composable
// ============================================

@Composable
fun DiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: ThemeColor = ThemeColor.ORANGE,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> getDarkColorScheme(themeColor)
        else -> getLightColorScheme(themeColor)
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}