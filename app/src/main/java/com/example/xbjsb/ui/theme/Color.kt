package com.example.xbjsb.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================
// 主题色方案 - 5 种风格
// ============================================

// 🍊 橙子主题（默认）- 温暖活力
object OrangeTheme {
    val primary = Color(0xFFFF6B35)
    val primaryContainer = Color(0xFFFFE5DD)
    val secondary = Color(0xFFFFB562)
    val secondaryContainer = Color(0xFFFFF4E6)
    
    val primaryDark = Color(0xFFFFB895)
    val primaryContainerDark = Color(0xFF5C2912)
    val secondaryDark = Color(0xFFFFD699)
    val secondaryContainerDark = Color(0xFF4E3619)
}

// 💙 蓝色主题 - 清新专注
object BlueTheme {
    val primary = Color(0xFF2196F3)
    val primaryContainer = Color(0xFFE3F2FD)
    val secondary = Color(0xFF64B5F6)
    val secondaryContainer = Color(0xFFE3F2FD)
    
    val primaryDark = Color(0xFF90CAF9)
    val primaryContainerDark = Color(0xFF0D47A1)
    val secondaryDark = Color(0xFF82BFF4)
    val secondaryContainerDark = Color(0xFF12344D)
}

// 💜 紫色主题 - 优雅浪漫
object PurpleTheme {
    val primary = Color(0xFF9C27B0)
    val primaryContainer = Color(0xFFF3E5F5)
    val secondary = Color(0xFFBA68C8)
    val secondaryContainer = Color(0xFFEDE7F6)
    
    val primaryDark = Color(0xFFCE93D8)
    val primaryContainerDark = Color(0xFF4A148C)
    val secondaryDark = Color(0xFFB39DDB)
    val secondaryContainerDark = Color(0xFF311B92)
}

// 💚 绿色主题 - 自然平静
object GreenTheme {
    val primary = Color(0xFF4CAF50)
    val primaryContainer = Color(0xFFE8F5E9)
    val secondary = Color(0xFF66BB6A)
    val secondaryContainer = Color(0xFFF1F8E9)
    
    val primaryDark = Color(0xFF81C784)
    val primaryContainerDark = Color(0xFF1B5E20)
    val secondaryDark = Color(0xFFC5E1A5)
    val secondaryContainerDark = Color(0xFF33691E)
}

// 🌸 粉色主题 - 温柔可爱
object PinkTheme {
    val primary = Color(0xFFE91E63)
    val primaryContainer = Color(0xFFFCE4EC)
    val secondary = Color(0xFFEC407A)
    val secondaryContainer = Color(0xFFFFF3E0)
    
    val primaryDark = Color(0xFFF48FB1)
    val primaryContainerDark = Color(0xFF880E4F)
    val secondaryDark = Color(0xFFFFCC80)
    val secondaryContainerDark = Color(0xFFE65100)
}

// ============================================
// 中性色系统
// ============================================

object NeutralColors {
    // 浅色模式中性色
    val surfaceLight = Color(0xFFFFFBFF)
    val surfaceVariantLight = Color(0xFFE8E8E8)     // 纯灰，更干净
    val onSurfaceLight = Color(0xFF1C1B1F)
    val onSurfaceVariantLight = Color(0xFF49454F)
    val outlineLight = Color(0xFF79747E)
    val outlineVariantLight = Color(0xFFCAC4D0)
    
    // 深色模式中性色（保持不变）
    val surfaceDark = Color(0xFF1C1B1F)
    val surfaceVariantDark = Color(0xFF49454F)
    val onSurfaceDark = Color(0xFFE6E1E5)
    val onSurfaceVariantDark = Color(0xFFCAC4D0)
    val outlineDark = Color(0xFF938F99)
    val outlineVariantDark = Color(0xFF49454F)
    
    // 背景色
    val backgroundLight = Color(0xFFFCFCFC)          // 纯净浅灰背景
    val backgroundDark = Color(0xFF1C1B1F)
}

// ============================================
// 心情颜色（浅色背景用）
// ============================================

object MoodColors {
    val happy = Color(0xFFFFE082)        // 温暖橙（加深）
    val calm = Color(0xFFBBDEFB)         // 清澈蓝（加深）
    val excited = Color(0xFFF8BBD0)      // 活力粉（加深）
    val sad = Color(0xFFC5CAE9)          // 忧郁紫（加深）
    val neutral = Color(0xFFE0E0E0)      // 中性灰（加深，避免太淡）
    
    // 深色模式心情颜色（保持不变）
    val happyDark = Color(0xFF4A3A2A)
    val calmDark = Color(0xFF2A3A4A)
    val excitedDark = Color(0xFF4A2A3A)
    val sadDark = Color(0xFF3A2A4A)
    val neutralDark = Color(0xFF3A3A3A)
}

// ============================================
// 功能性颜色
// ============================================

object FunctionalColors {
    // 错误色
    val error = Color(0xFFB3261E)
    val errorContainer = Color(0xFFF9DEDC)
    val onError = Color.White
    val onErrorContainer = Color(0xFF410E0B)
    
    val errorDark = Color(0xFFF2B8B5)
    val errorContainerDark = Color(0xFF8C1D18)
    val onErrorDark = Color(0xFF601410)
    val onErrorContainerDark = Color(0xFFF9DEDC)
    
    // 警告色
    val warning = Color(0xFFF57C00)
    val warningContainer = Color(0xFFFFE0B2)
    val warningDark = Color(0xFFFFB74D)
    val warningContainerDark = Color(0xFF5D4037)
    
    // 成功色
    val success = Color(0xFF2E7D32)
    val successContainer = Color(0xFFC8E6C9)
    val successDark = Color(0xFF66BB6A)
    val successContainerDark = Color(0xFF1B5E20)
}

// ============================================
// 特殊效果颜色
// ============================================

object EffectColors {
    val scrim = Color(0x99000000)          // 遮罩层
    val shadow = Color(0x33000000)         // 阴影
    val divider = Color(0x1F000000)        // 分割线
    val shimmer = Color(0x33FFFFFF)        // 闪光
    
    val scrimDark = Color(0xB3000000)
    val shadowDark = Color(0x66000000)
    val dividerDark = Color(0x33FFFFFF)
    val shimmerDark = Color(0x1AFFFFFF)
}

// ============================================
// 渐变色组合
// ============================================

object GradientColors {
    // 心情渐变
    val Happy = listOf(
        Color(0xFFFFD54F),
        Color(0xFFFFB74D)
    )
    
    val Sad = listOf(
        Color(0xFF64B5F6),
        Color(0xFF7986CB)
    )
    
    val Excited = listOf(
        Color(0xFFFF7043),
        Color(0xFFEF5350)
    )
    
    val Calm = listOf(
        Color(0xFF81C784),
        Color(0xFF4DB6AC)
    )
    
    val Neutral = listOf(
        Color(0xFFBDBDBD),
        Color(0xFF9E9E9E)
    )
}

// ============================================
// 主题枚举
// ============================================

enum class ThemeColor {
    ORANGE,    // 橙子主题（默认）
    BLUE,      // 蓝色主题
    PURPLE,    // 紫色主题
    GREEN,     // 绿色主题
    PINK       // 粉色主题
}