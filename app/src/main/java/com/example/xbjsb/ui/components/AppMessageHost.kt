package com.example.xbjsb.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

sealed class AppMessageType(val icon: ImageVector) {
    object Success : AppMessageType(Icons.Filled.CheckCircle)
    object Info : AppMessageType(Icons.Filled.Info)
    object Warning : AppMessageType(Icons.Filled.Warning)
    object Error : AppMessageType(Icons.Filled.Error)
    object Private : AppMessageType(Icons.Filled.Lock)
}

data class AppMessage(
    val text: String,
    val type: AppMessageType = AppMessageType.Info,
    val durationMillis: Long = 1800L,
    val id: Long = System.nanoTime()
)

@Stable
class AppMessageHostState {
    var currentMessage by mutableStateOf<AppMessage?>(null)
        private set

    fun show(
        text: String,
        type: AppMessageType = AppMessageType.Info,
        durationMillis: Long = 1800L
    ) {
        currentMessage = AppMessage(text, type, durationMillis)
    }

    fun dismiss() {
        currentMessage = null
    }
}

@Composable
fun rememberAppMessageHostState(): AppMessageHostState = remember { AppMessageHostState() }

val LocalAppMessageHostState: ProvidableCompositionLocal<AppMessageHostState?> = compositionLocalOf { null }

@Composable
fun AppMessageHost(
    hostState: AppMessageHostState,
    modifier: Modifier = Modifier
) {
    val message = hostState.currentMessage
    LaunchedEffect(message) {
        message?.let { current ->
            delay(current.durationMillis)
            if (hostState.currentMessage == current) {
                hostState.dismiss()
            }
        }
    }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = message != null,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = spring(
                    dampingRatio = 0.78f,
                    stiffness = 360f
                )
            ) + fadeIn(animationSpec = tween(180)) + scaleIn(
                initialScale = 0.94f,
                transformOrigin = TransformOrigin(0.5f, 1f),
                animationSpec = spring(
                    dampingRatio = 0.78f,
                    stiffness = 360f
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { it / 3 },
                animationSpec = tween(230)
            ) + fadeOut(animationSpec = tween(220)) + scaleOut(
                targetScale = 0.965f,
                transformOrigin = TransformOrigin(0.5f, 1f),
                animationSpec = tween(230)
            )
        ) {
            message?.let { current ->
                val accent = when (current.type) {
                    AppMessageType.Success -> MaterialTheme.colorScheme.primary
                    AppMessageType.Info -> MaterialTheme.colorScheme.secondary
                    AppMessageType.Warning -> MaterialTheme.colorScheme.tertiary
                    AppMessageType.Error -> MaterialTheme.colorScheme.error
                    AppMessageType.Private -> MaterialTheme.colorScheme.primary
                }
                val container = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                val content = MaterialTheme.colorScheme.onSurface
                val outline = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f)

                Surface(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 18.dp)
                        .widthIn(max = 360.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(999.dp),
                            clip = false
                        ),
                    shape = RoundedCornerShape(999.dp),
                    color = container,
                    tonalElevation = 2.dp,
                    border = BorderStroke(1.dp, outline)
                ) {
                    Row(
                        modifier = Modifier.padding(start = 10.dp, top = 8.dp, end = 14.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .background(
                                    color = accent.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(999.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = current.type.icon,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = current.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = content
                        )
                    }
                }
            }
        }
    }
}
