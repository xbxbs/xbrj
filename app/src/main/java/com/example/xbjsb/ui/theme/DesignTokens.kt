package com.example.xbjsb.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Material 3 Expression Design Tokens
 * 统一的设计变量，确保视觉一致性
 */

// 间距系统（基于 8dp 网格）
object Spacing {
    val XXS = 2.dp
    val XS = 4.dp
    val S = 8.dp
    val M = 12.dp
    val L = 16.dp
    val XL = 24.dp
    val XXL = 32.dp
    val XXXL = 48.dp
    val Huge = 64.dp
}

// 圆角系统
object CornerRadius {
    val None = 0.dp
    val XSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val XLarge = 20.dp
    val XXLarge = 28.dp
    val Full = 9999.dp  // 完全圆形
}

// 动画时长（毫秒）- 针对高刷新率屏幕优化
object Duration {
    const val Instant = 0
    const val Quick = 200      // 从 150 增加到 200
    const val Fast = 250       // 从 200 增加到 250
    const val Standard = 400   // 从 300 增加到 400
    const val Slow = 500       // 从 400 增加到 500
    const val VerySlow = 600   // 从 500 增加到 600
    const val Sluggish = 800   // 从 700 增加到 800
}

// 阴影层级
object Elevation {
    val None = 0.dp
    val XXSmall = 1.dp
    val XSmall = 2.dp
    val Small = 4.dp
    val Medium = 6.dp
    val Large = 8.dp
    val XLarge = 12.dp
    val XXLarge = 16.dp
    val Huge = 24.dp
}

// 透明度
object Alpha {
    const val Invisible = 0f
    const val Disabled = 0.38f
    const val Medium = 0.6f
    const val High = 0.87f
    const val Surface = 0.95f
    const val AlmostOpaque = 0.98f
    const val Opaque = 1f
}

// 模糊半径
object BlurRadius {
    val None = 0.dp
    val Small = 8.dp
    val Medium = 16.dp
    val Large = 24.dp
    val XLarge = 32.dp
}

// 图标尺寸
object IconSize {
    val XSmall = 12.dp
    val Small = 16.dp
    val Medium = 24.dp
    val Large = 32.dp
    val XLarge = 48.dp
    val XXLarge = 64.dp
}

// 卡片规格
object CardSpec {
    val PaddingHorizontal = 20.dp
    val PaddingVertical = 16.dp
    val SpacingBetween = 12.dp
    val MinHeight = 120.dp
    val MaxHeight = 300.dp
}

// 页面边距
object PagePadding {
    val Horizontal = 20.dp
    val Top = 16.dp
    val Bottom = 16.dp
    val HorizontalCompact = 16.dp  // 小屏设备
}

// 组件尺寸
object ComponentSize {
    val ButtonHeight = 48.dp
    val ButtonHeightSmall = 40.dp
    val ButtonHeightLarge = 56.dp
    val IconButtonSize = 40.dp
    val FABSize = 56.dp
    val FABSizeSmall = 40.dp
    val FABSizeLarge = 96.dp
    val TopBarHeight = 64.dp
    val BottomBarHeight = 80.dp
}

// 动画缩放系数
object ScaleFactor {
    const val Pressed = 0.95f
    const val PressedLarge = 0.92f
    const val Hover = 1.02f
    const val Selected = 1.05f
    const val Emphasized = 1.1f
    const val Pop = 1.3f
}

// 边框宽度
object BorderWidth {
    val Hairline = 0.5.dp
    val Thin = 1.dp
    val Medium = 2.dp
    val Thick = 4.dp
}

// Z轴层级（用于排序）
object ZIndex {
    const val Background = 0f
    const val Content = 1f
    const val Card = 2f
    const val FloatingButton = 3f
    const val TopBar = 4f
    const val Dialog = 5f
    const val Snackbar = 6f
    const val Tooltip = 7f
}