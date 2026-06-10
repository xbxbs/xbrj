package com.example.xbjsb.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expression 模糊效果系统
 * 提供跨版本兼容的模糊背景效果
 */

/**
 * 毛玻璃背景效果
 * @param radius 模糊半径
 * @param backgroundColor 背景颜色（带透明度）
 * @param borderColor 边框颜色（可选）
 * @param borderWidth 边框宽度
 */
fun Modifier.glassmorphic(
    radius: Dp = BlurRadius.Medium,
    backgroundColor: Color = NeutralColors.surfaceLight.copy(alpha = 0.95f),
    borderColor: Color? = Color.White.copy(alpha = 0.2f),
    borderWidth: Dp = BorderWidth.Hairline
): Modifier {
    return this
        .blur(radius = radius, edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
        .background(backgroundColor)
        .then(
            if (borderColor != null) {
                Modifier.drawBehind {
                    drawRect(
                        color = borderColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = borderWidth.toPx()
                        )
                    )
                }
            } else Modifier
        )
}

/**
 * 渐变模糊背景
 * @param colors 渐变色列表
 * @param blurRadius 模糊半径
 * @param alpha 整体透明度
 */
fun Modifier.gradientBlur(
    colors: List<Color>,
    blurRadius: Dp = BlurRadius.Medium,
    alpha: Float = Alpha.Surface
): Modifier {
    return this
        .blur(radius = blurRadius, edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
        .background(
            Brush.verticalGradient(
                colors = colors.map { it.copy(alpha = alpha) }
            )
        )
}

/**
 * 条件模糊（根据 API 级别自动适配）
 * API 31+ 使用原生模糊
 * 低版本使用半透明层模拟
 */
fun Modifier.conditionalBlur(
    radius: Dp = BlurRadius.Medium,
    fallbackColor: Color = Color.White.copy(alpha = 0.9f)
): Modifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.blur(radius = radius)
    } else {
        this.background(fallbackColor)
    }
}

/**
 * 卡片毛玻璃效果
 * 专门为卡片组件优化的模糊效果
 */
fun Modifier.cardGlassmorphic(
    isElevated: Boolean = false
): Modifier {
    val blurRadius = if (isElevated) BlurRadius.Large else BlurRadius.Medium
    val backgroundColor = if (isElevated) {
        NeutralColors.surfaceLight.copy(alpha = 0.98f)
    } else {
        NeutralColors.surfaceLight.copy(alpha = 0.95f)
    }
    
    return this.glassmorphic(
        radius = blurRadius,
        backgroundColor = backgroundColor,
        borderColor = Color.White.copy(alpha = if (isElevated) 0.3f else 0.2f)
    )
}

/**
 * 顶栏毛玻璃效果
 */
fun Modifier.topBarGlassmorphic(): Modifier {
    return this.glassmorphic(
        radius = BlurRadius.Large,
        backgroundColor = NeutralColors.surfaceLight.copy(alpha = 0.92f),
        borderColor = Color.White.copy(alpha = 0.1f)
    )
}

/**
 * 底部导航栏毛玻璃效果
 */
fun Modifier.bottomBarGlassmorphic(): Modifier {
    return this.glassmorphic(
        radius = BlurRadius.Large,
        backgroundColor = NeutralColors.surfaceLight.copy(alpha = 0.95f),
        borderColor = Color.White.copy(alpha = 0.15f)
    )
}

/**
 * 浮动按钮毛玻璃效果
 */
fun Modifier.fabGlassmorphic(): Modifier {
    return this.glassmorphic(
        radius = BlurRadius.Small,
        backgroundColor = NeutralColors.surfaceLight.copy(alpha = 0.9f),
        borderColor = Color.White.copy(alpha = 0.3f),
        borderWidth = BorderWidth.Thin
    )
}

/**
 * 对话框背景模糊
 */
fun Modifier.dialogBlur(): Modifier {
    return this.glassmorphic(
        radius = BlurRadius.XLarge,
        backgroundColor = Color.Black.copy(alpha = 0.6f),
        borderColor = null
    )
}

/**
 * 遮罩层模糊（用于弹出层背景）
 */
fun Modifier.scrimBlur(): Modifier {
    return this.glassmorphic(
        radius = BlurRadius.Large,
        backgroundColor = EffectColors.scrim,
        borderColor = null
    )
}

/**
 * 模拟 iOS 风格的模糊效果（低版本兼容）
 * 使用多层半透明渐变叠加
 */
fun Modifier.iosStyleBlur(
    alpha: Float = 0.9f
): Modifier {
    return this.background(
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = alpha),
                Color.White.copy(alpha = alpha * 0.95f),
                Color.White.copy(alpha = alpha * 0.9f)
            )
        )
    )
}