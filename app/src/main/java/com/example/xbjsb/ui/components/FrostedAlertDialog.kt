package com.example.xbjsb.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 带模糊效果的 AlertDialog 封装
 * 自动在 title 里应用 ApplyFrostedDialogWindow
 */
@Composable
fun FrostedAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: String? = null,
    text: @Composable (() -> Unit)? = null,
    frostedBlurEnabled: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        icon = icon,
        title = if (title != null) {
            {
                ApplyFrostedDialogWindow(enabled = frostedBlurEnabled)
                Text(title)
            }
        } else null,
        text = text
    )
}

/**
 * 支持自定义 title Composable 的版本
 */
@Composable
fun FrostedAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    titleContent: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    frostedBlurEnabled: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        icon = icon,
        title = if (titleContent != null) {
            {
                ApplyFrostedDialogWindow(enabled = frostedBlurEnabled)
                titleContent()
            }
        } else null,
        text = text
    )
}
