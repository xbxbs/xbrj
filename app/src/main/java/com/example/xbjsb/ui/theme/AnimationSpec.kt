package com.example.xbjsb.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset

/**
 * 克制、统一、120Hz 友好的动效系统。
 * 原则：低弹性、小缩放、节奏一致，避免鬼畜。
 */
object Motion {
    const val Micro = 180
    const val Fast = 260
    const val Standard = 340
    const val Filter = 420
    const val Page = 520
    const val Exit = 260

    const val PressedScale = 0.985f
    const val ChipPressedScale = 0.975f
    const val FavoriteScale = 1.08f

    // 方向形变：幅度必须克制，避免廉价果冻感
    const val PageEnterScale = 1.012f
    const val PageBackgroundScale = 0.988f
    const val VerticalEnterScaleY = 0.92f
    const val VerticalExitScaleY = 0.96f
    const val ListExitScaleY = 0.92f
    const val ListExitScaleX = 1.01f
}

object MotionEasing {
    val Standard = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
    val Smooth = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val Filter = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
    val Exit = CubicBezierEasing(0.4f, 0f, 1f, 1f)
}

object MotionSpec {
    fun <T> softSpring(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.88f,
        stiffness = 300f
    )

    fun <T> smoothSpring(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.94f,
        stiffness = 260f
    )

    fun <T> quickSpring(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.86f,
        stiffness = 420f
    )

    fun <T> standardTween(duration: Int = Motion.Standard): TweenSpec<T> = tween(
        durationMillis = duration,
        easing = MotionEasing.Smooth
    )

    fun <T> exitTween(duration: Int = Motion.Exit): TweenSpec<T> = tween(
        durationMillis = duration,
        easing = MotionEasing.Exit
    )

    fun floatSoft(): FiniteAnimationSpec<Float> = softSpring()
    fun floatQuick(): FiniteAnimationSpec<Float> = quickSpring()
    fun dpSmooth(): FiniteAnimationSpec<Dp> = smoothSpring()
    fun offsetSoft(): FiniteAnimationSpec<IntOffset> = softSpring()
}

object PageTransitions {
    private const val BackgroundShiftFraction = 0.18f  // 背景联动位移 18%，保证视差明显
    
    // 从右进入新页面（首页 → 详情/编辑）
    // 法则：横向运动 + 轻微透明度变化，避免明显过冲
    val Enter: EnterTransition = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = spring(
            dampingRatio = 0.92f,
            stiffness = 360f
        )
    ) + fadeIn(
        animationSpec = tween(340, easing = MotionEasing.Smooth)
    ) + scaleIn(
        initialScale = Motion.PageEnterScale,
        animationSpec = tween(340, easing = MotionEasing.Smooth)
    )

    // 旧页面退到背景（首页轻微左移 + 淡出）
    // 视差联动：新页面右滑，背景被推向左侧 10%
    val Exit: ExitTransition = slideOutHorizontally(
        targetOffsetX = { (-it * BackgroundShiftFraction).toInt() },  // 负数=左移
        animationSpec = spring(
            dampingRatio = 0.85f,
            stiffness = 450f
        )
    ) + fadeOut(
        targetAlpha = 0.72f,
        animationSpec = tween(380, easing = MotionEasing.Filter)
    ) + scaleOut(
        targetScale = Motion.PageBackgroundScale,
        animationSpec = tween(380, easing = MotionEasing.Smooth)
    )

    // 返回时从背景恢复（首页从左回正 + 淡入）
    // 视差联动：详情页右退，背景从左平稳恢复原位
    val PopEnter: EnterTransition = slideInHorizontally(
        initialOffsetX = { (-it * BackgroundShiftFraction).toInt() },  // 从左侧恢复
        animationSpec = spring(
            dampingRatio = 0.94f,
            stiffness = 360f
        )
    ) + fadeIn(
        initialAlpha = 0.72f,
        animationSpec = tween(360, easing = MotionEasing.Smooth)
    ) + scaleIn(
        initialScale = Motion.PageBackgroundScale,
        animationSpec = tween(360, easing = MotionEasing.Smooth)
    )

    // 详情/编辑页向右退出
    // 法则：横向位移 + 淡出，避免返回末尾回弹
    val PopExit: ExitTransition = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = spring(
            dampingRatio = 0.94f,
            stiffness = 360f
        )
    ) + fadeOut(
        animationSpec = tween(340, easing = MotionEasing.Smooth)
    )
}

object EnterTransitions {
    val FadeInScale = androidx.compose.animation.fadeIn(
        animationSpec = tween(Motion.Standard, easing = MotionEasing.Smooth)
    ) + androidx.compose.animation.scaleIn(
        initialScale = 0.992f,
        animationSpec = tween(Motion.Standard, easing = MotionEasing.Smooth)
    )

    val FadeInSlideUp = androidx.compose.animation.fadeIn(
        animationSpec = tween(Motion.Standard, easing = MotionEasing.Smooth)
    ) + androidx.compose.animation.slideInVertically(
        initialOffsetY = { it / 16 },
        animationSpec = tween(Motion.Standard, easing = MotionEasing.Smooth)
    )

    val FadeInExpand = androidx.compose.animation.fadeIn(
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 380f
        )
    ) + androidx.compose.animation.scaleIn(
        initialScale = 0.98f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = 300f
        )
    ) + androidx.compose.animation.expandVertically(
        expandFrom = Alignment.Top,
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = 380f
        )
    )
}

object ExitTransitions {
    val FadeOutScale = androidx.compose.animation.fadeOut(
        animationSpec = tween(Motion.Exit, easing = MotionEasing.Exit)
    ) + androidx.compose.animation.scaleOut(
        targetScale = 0.992f,
        animationSpec = tween(Motion.Exit, easing = MotionEasing.Exit)
    )

    val FadeOutSlideDown = androidx.compose.animation.fadeOut(
        animationSpec = tween(Motion.Exit, easing = MotionEasing.Exit)
    ) + androidx.compose.animation.slideOutVertically(
        targetOffsetY = { it / 16 },
        animationSpec = tween(Motion.Exit, easing = MotionEasing.Exit)
    )

    val FadeOutShrink = androidx.compose.animation.fadeOut(
        animationSpec = tween(220, easing = MotionEasing.Exit)
    ) + androidx.compose.animation.scaleOut(
        targetScale = 0.992f,
        animationSpec = tween(220, easing = MotionEasing.Exit)
    ) + androidx.compose.animation.shrinkVertically(
        shrinkTowards = Alignment.Top,
        animationSpec = tween(220, easing = MotionEasing.Exit)
    )
}

// 兼容旧引用，逐步迁移使用 MotionSpec。
object Easing {
    val Standard = MotionEasing.Standard
    val Emphasized = MotionEasing.Smooth
    val Decelerate = MotionEasing.Smooth
    val Accelerate = MotionEasing.Exit
    val Smooth = MotionEasing.Smooth
}

object SpringPreset {
    val Soft = MotionSpec.floatSoft()
    val Standard = MotionSpec.floatQuick()
    val Quick = MotionSpec.floatQuick()
    val Smooth = MotionSpec.floatSoft()
}