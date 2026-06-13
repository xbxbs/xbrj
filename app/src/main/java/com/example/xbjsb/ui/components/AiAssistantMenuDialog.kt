package com.example.xbjsb.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.xbjsb.ai.DiaryAiAction

sealed class AiMenuItem(
    val title: String,
    val description: String,
    val needsContent: Boolean,
    val action: DiaryAiAction
) {
    object QuickGenerate : AiMenuItem("快速生成", "读取关键词扩展成完整日记", false, DiaryAiAction.QUICK_GENERATE)
    object Expand : AiMenuItem("扩写", "增加细节和描写", true, DiaryAiAction.EXPAND)
    object Continue : AiMenuItem("续写", "基于已有内容继续写", true, DiaryAiAction.CONTINUE)
    object Shorten : AiMenuItem("缩写", "精简内容保留核心", true, DiaryAiAction.SHORTEN)
    
    object Polish : AiMenuItem("润色", "优化表达和结构", true, DiaryAiAction.POLISH)
    object GenerateTitle : AiMenuItem("标题生成", "根据正文生成标题", true, DiaryAiAction.GENERATE_TITLE)
    
    object AnalyzeEmotion : AiMenuItem("情绪分析", "分析并设置心情", true, DiaryAiAction.SUMMARIZE_EMOTION)
    object RecommendTags : AiMenuItem("标签推荐", "推荐标签和分组", true, DiaryAiAction.RECOMMEND_TAGS_GROUP)
}

data class AiMenuSection(
    val title: String,
    val items: List<AiMenuItem>
)

@Composable
fun AiAssistantMenuDialog(
    hasContent: Boolean,
    isGenerating: Boolean,
    frostedBlurEnabled: Boolean,
    onDismiss: () -> Unit,
    onActionSelect: (DiaryAiAction) -> Unit,
    onQaTemplateSelect: () -> Unit
) {
    val sections = listOf(
        AiMenuSection("智能写作", listOf(
            AiMenuItem.QuickGenerate,
            AiMenuItem.Expand,
            AiMenuItem.Continue,
            AiMenuItem.Shorten
        )),
        AiMenuSection("优化润色", listOf(
            AiMenuItem.Polish,
            AiMenuItem.GenerateTitle
        )),
        AiMenuSection("辅助工具", listOf(
            AiMenuItem.AnalyzeEmotion,
            AiMenuItem.RecommendTags
        ))
    )
    
    Dialog(onDismissRequest = onDismiss) {
        ApplyFrostedDialogWindow(enabled = frostedBlurEnabled)
        
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "AI 助手",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(Modifier.height(18.dp))
                
                sections.forEach { section ->
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    )
                    Spacer(Modifier.height(10.dp))
                    
                    section.items.forEach { item ->
                        AiMenuItemRow(
                            item = item,
                            enabled = (!item.needsContent || hasContent) && !isGenerating,
                            onClick = { 
                                onActionSelect(item.action)
                                onDismiss()
                            }
                        )
                    }
                    
                    Spacer(Modifier.height(14.dp))
                }
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
                Spacer(Modifier.height(14.dp))
                
                Text(
                    text = "问答式写作",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                )
                Spacer(Modifier.height(10.dp))
                
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onQaTemplateSelect()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGenerating,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isGenerating) 
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        else 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                ) {
                    Text("选择模板开始写作")
                }
            }
        }
    }
}

@Composable
private fun AiMenuItemRow(
    item: AiMenuItem,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (enabled) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    }
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    }
                )
            }
            
            if (enabled) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
    
    Spacer(Modifier.height(8.dp))
}
