package com.example.xbjsb.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * 按钮弹性点击效果
 * 法则：按下轻微缩小 → 释放过冲放大 → 回弹到正常
 */
fun Modifier.bounceClick(
    onClick: () -> Unit
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 400f
        ),
        label = "bounce"
    )
    
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) {
            isPressed = true
            onClick()
            scope.launch {
                kotlinx.coroutines.delay(100)
                isPressed = false
            }
        }
}

/**
 * 优化的 Snackbar - 保留默认样式 + 添加滑入动效
 */
@Composable
fun EnhancedSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    var currentData by remember { mutableStateOf<SnackbarData?>(null) }
    
    LaunchedEffect(hostState.currentSnackbarData) {
        currentData = hostState.currentSnackbarData
    }
    
    val offsetY by animateDpAsState(
        targetValue = if (currentData != null) 0.dp else 60.dp,
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = 380f
        ),
        label = "snackbar_slide"
    )
    
    Box(
        modifier = modifier.offset(y = offsetY)
    ) {
        SnackbarHost(hostState = hostState)
    }
}
