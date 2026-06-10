package com.example.xbjsb.ui.components

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.WindowManager
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider

/**
 * Applies Android 12+ window-level blur behind the current Dialog window.
 *
 * This is real system blur, but it only works for separate windows such as Dialog/Popup.
 * Normal composables inside the Activity window, including TopAppBar, cannot use
 * FLAG_BLUR_BEHIND because they do not own a separate Window surface.
 */
@Composable
fun ApplyFrostedDialogWindow(
    enabled: Boolean,
    radius: Int = 48,
    dimAmount: Float = 0.15f
) {
    val dialogView = LocalView.current
    
    // 用 Compose 动画平滑过渡透明度
    var targetDim by remember { mutableStateOf(0.18f) }
    val animatedDim by androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetDim,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 200,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "dialog_dim"
    )

    DisposableEffect(dialogView, enabled, radius, dimAmount) {
        val window = (dialogView.parent as? DialogWindowProvider)?.window
        val previousDimAmount = window?.attributes?.dimAmount
        val previousBackground = window?.decorView?.background

        if (window != null) {
            window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && enabled) {
                // 立即启动模糊
                window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                window.attributes = window.attributes.apply {
                    blurBehindRadius = radius
                }
                
                // 60ms 后触发降低 dim 的动画
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    targetDim = dimAmount
                }, 60)
                
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                window.attributes = window.attributes.apply {
                    blurBehindRadius = 0
                    this.dimAmount = previousDimAmount ?: this.dimAmount
                }
            }
        }

        onDispose {
            if (window != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                window.attributes = window.attributes.apply {
                    blurBehindRadius = 0
                    if (previousDimAmount != null) this.dimAmount = previousDimAmount
                }
                window.setBackgroundDrawable(previousBackground)
            }
        }
    }
    
    // 持续应用动画后的 dimAmount
    LaunchedEffect(animatedDim) {
        val window = (dialogView.parent as? DialogWindowProvider)?.window
        if (window != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && enabled) {
            window.attributes = window.attributes.apply {
                this.dimAmount = animatedDim
            }
        }
    }
}

/**
 * Visual frosted material for Activity top bars.
 *
 * This is intentionally not real blur: TopAppBar is not a separate Android Window.
 * It provides the same soft translucent surface treatment across secondary pages.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun frostedTopAppBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
    titleContentColor = MaterialTheme.colorScheme.onSurface,
    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
)
