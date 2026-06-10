package com.example.xbjsb.ai

import com.example.xbjsb.data.AiPreferences
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object DiaryQaTemplates {
    val templates: List<DiaryQaTemplate> = listOf(
        DiaryQaTemplate(
            id = "daily",
            name = "每日记录",
            description = "适合把普通一天整理成自然日记",
            questions = listOf(
                DiaryQaQuestion("event", "今天最值得记录的一件事是什么？", "例如：今天完成了一次考试/和朋友聊了很久/终于休息了一会"),
                DiaryQaQuestion("scene", "这件事发生在什么场景？", "时间、地点、人物、当时的氛围"),
                DiaryQaQuestion("feeling", "你当时最明显的感受是什么？", "开心、焦虑、平静、疲惫、兴奋……"),
                DiaryQaQuestion("thought", "这件事让你想到什么？", "一个想法、反思或突然意识到的事情"),
                DiaryQaQuestion("ending", "你希望怎样记住今天？", "给今天留下一句话"),
            ),
            styleHint = "自然、真诚、有生活感，不要过度文学化"
        ),
        DiaryQaTemplate(
            id = "emotion_review",
            name = "情绪复盘",
            description = "适合整理焦虑、低落、压力或关系事件",
            questions = listOf(
                DiaryQaQuestion("emotion", "今天最强烈的情绪是什么？", "例如：焦虑、委屈、烦躁、失落、轻松"),
                DiaryQaQuestion("trigger", "是什么事情触发了这种情绪？", "尽量描述具体事件"),
                DiaryQaQuestion("belief", "当时你脑子里出现了什么想法？", "例如：我是不是做不好、别人是不是不理解我"),
                DiaryQaQuestion("body", "身体上有什么反应？", "心跳、疲惫、紧绷、想哭、睡不着等"),
                DiaryQaQuestion("reframe", "如果换一个更温和的角度看这件事，你会怎么理解？", "写给自己的安慰或提醒"),
            ),
            styleHint = "温和、克制、带一点心理复盘感，不说教"
        ),
        DiaryQaTemplate(
            id = "study_review",
            name = "学习复盘",
            description = "适合学生记录学习、考试、项目进展",
            questions = listOf(
                DiaryQaQuestion("learned", "今天主要学了什么？", "课程、知识点、练习、项目内容"),
                DiaryQaQuestion("important", "哪个知识点或收获最重要？", "用自己的话解释一下"),
                DiaryQaQuestion("stuck", "哪里卡住了？", "不会的题、没理解的概念、效率问题"),
                DiaryQaQuestion("solution", "你尝试了什么解决方法？", "查资料、问人、复盘错题、换方法"),
                DiaryQaQuestion("next", "下一步准备怎么做？", "明天或下一次的具体行动"),
            ),
            styleHint = "清晰、理性、有复盘感，适合长期学习记录"
        ),
        DiaryQaTemplate(
            id = "goal_review",
            name = "目标复盘",
            description = "适合记录目标推进、效率和下一步行动",
            questions = listOf(
                DiaryQaQuestion("goal", "今天原本的目标是什么？", "计划完成什么"),
                DiaryQaQuestion("done", "实际完成了什么？", "完成情况和结果"),
                DiaryQaQuestion("good", "哪里做得比较好？", "值得保留的方法"),
                DiaryQaQuestion("block", "哪里被卡住或偏离了？", "原因是什么"),
                DiaryQaQuestion("action", "明天最重要的一步是什么？", "尽量具体到一个行动"),
            ),
            styleHint = "简洁、务实、有行动感"
        )
    )
}

data class DiaryQaTemplate(
    val id: String,
    val name: String,
    val description: String,
    val questions: List<DiaryQaQuestion>,
    val styleHint: String
)

data class DiaryQaQuestion(
    val id: String,
    val question: String,
    val placeholder: String,
    val required: Boolean = true
)

data class DiaryQaAnswer(
    val question: String,
    val answer: String
)

data class GeneratedDiaryResult(
    val title: String = "",
    val content: String = "",
    val mood: String = "neutral",
    val tags: List<String> = emptyList(),
    val group: String = "",
    val summary: String = "",
    val writingStyle: String = ""
)

data class DiaryGenerationContext(
    val dateText: String,
    val existingTitle: String = "",
    val existingContent: String = "",
    val currentMood: String = "neutral",
    val currentTags: List<String> = emptyList(),
    val currentGroup: String = "",
    val lengthPreference: String = "中，约 300-500 字",
    val stylePreference: String = "自然真实，像普通人认真写下的一篇日记"
)

enum class DiaryAiAction(
    val displayName: String,
    val shortName: String
) {
    EXPAND("AI 扩写", "扩写"),
    SHORTEN("AI 缩写", "缩写"),
    POLISH("AI 润色", "润色"),
    CONTINUE("AI 续写", "续写"),
    GENERATE_TITLE("标题生成", "标题"),
    SUMMARIZE_EMOTION("情绪总结", "情绪"),
    RECOMMEND_TAGS_GROUP("标签 / 分组推荐", "标签分组")
}

class OpenAiCompatibleDiaryService(
    private val gson: Gson = Gson()
) {
    suspend fun generateDiaryFromQa(
        config: AiPreferences.AiConfig,
        template: DiaryQaTemplate,
        answers: List<DiaryQaAnswer>,
        generationContext: DiaryGenerationContext
    ): Result<GeneratedDiaryResult> = withContext(Dispatchers.IO) {
        runCatching {
            require(config.isConfigured) { "请先在设置中配置 AI API Key、Base URL 和模型" }

            val prompt = buildDiaryPrompt(template, answers, generationContext)
            val raw = chatCompletion(
                config = config,
                messages = listOf(
                    ChatMessage(
                        role = "system",
                        content = DIARY_SYSTEM_PROMPT
                    ),
                    ChatMessage(role = "user", content = prompt)
                )
            )
            parseGeneratedDiary(raw)
        }
    }

    suspend fun processDiaryWriting(
        config: AiPreferences.AiConfig,
        action: DiaryAiAction,
        generationContext: DiaryGenerationContext
    ): Result<GeneratedDiaryResult> = withContext(Dispatchers.IO) {
        runCatching {
            require(config.isConfigured) { "请先在设置中配置 AI API Key、Base URL 和模型" }
            require(generationContext.existingContent.isNotBlank()) { "请先写一点正文，再使用${action.shortName}" }

            val prompt = buildActionPrompt(action, generationContext)
            val raw = chatCompletion(
                config = config,
                messages = listOf(
                    ChatMessage(role = "system", content = DIARY_SYSTEM_PROMPT),
                    ChatMessage(role = "user", content = prompt)
                )
            )
            parseGeneratedDiary(raw)
        }
    }

    private fun buildDiaryPrompt(
        template: DiaryQaTemplate,
        answers: List<DiaryQaAnswer>,
        generationContext: DiaryGenerationContext
    ): String {
        val qaText = answers.mapIndexed { index, answer ->
            """
            ${index + 1}. 问题：${answer.question}
               回答：${answer.answer.ifBlank { "（未回答）" }}
            """.trimIndent()
        }.joinToString("\n\n")

        val mergeStrategy = if (generationContext.existingContent.isBlank()) {
            "生成新日记：根据模板问答生成一篇完整的新日记。"
        } else {
            "融合已有正文：保留已有正文的核心意思，将问答内容自然补充进去，使日记更完整、更流畅。"
        }

        val tagsText = if (generationContext.currentTags.isEmpty()) {
            "无"
        } else {
            generationContext.currentTags.joinToString("、")
        }

        return """
            请根据以下结构化信息，生成一篇中文日记。

            【当前日期】
            ${generationContext.dateText}

            【用户已有标题】
            ${generationContext.existingTitle.ifBlank { "无" }}

            【用户已有正文】
            ${generationContext.existingContent.ifBlank { "无" }}

            【当前心情】
            ${generationContext.currentMood.ifBlank { "neutral" }}

            【当前标签】
            $tagsText

            【当前分组】
            ${generationContext.currentGroup.ifBlank { "无" }}

            【选择的写作模板】
            模板名称：${template.name}
            模板说明：${template.description}
            模板目标：通过一组引导问题，帮助用户把零散经历、感受和思考整理成一篇真实自然的日记。
            推荐风格：${template.styleHint}

            【模板问题与用户回答】
            $qaText

            【输出偏好】
            目标长度：${generationContext.lengthPreference}
            语言风格：${generationContext.stylePreference}
            融合策略：$mergeStrategy

            生成要求：
            1. 使用第一人称，写得像用户本人当天写下的私人日记。
            2. 只能基于用户提供的信息整理、连接和润色，不得编造关键事实。
            3. 如果已有正文不为空，请自然融合已有正文和问答内容，不要简单重复。
            4. 如果问题未回答，请忽略，不要在正文中提到“未回答”。
            5. 正文需要包含事件、感受和一点自然的思考。
            6. 不要写成报告、作文、公众号、鸡汤或心理咨询建议。
            7. 不要过度煽情，不要过度升华，不要使用空泛套话。
            8. 自动生成标题、正文、心情、标签、分组、一句话摘要和风格说明。
            9. mood 只能是 happy、calm、excited、sad、neutral 之一。
            10. tags 必须是 2 到 5 个中文短标签，不带 #，每个标签不超过 6 个字。
            11. group 必须是一个简短中文分组，优先从生活、学习、情绪、目标、旅行、读书、工作、关系、健康、灵感中选择。
            12. 只返回严格 JSON，不要 Markdown，不要解释，不要代码块。

            返回格式：
            {
              "title": "日记标题",
              "content": "完整日记正文",
              "mood": "happy | calm | excited | sad | neutral",
              "tags": ["标签1", "标签2", "标签3"],
              "group": "分组",
              "summary": "一句话摘要",
              "writingStyle": "本次生成采用的风格说明"
            }
        """.trimIndent()
    }

    private fun buildActionPrompt(
        action: DiaryAiAction,
        generationContext: DiaryGenerationContext
    ): String {
        val tagsText = generationContext.currentTags.takeIf { it.isNotEmpty() }?.joinToString("、") ?: "无"
        val actionInstruction = when (action) {
            DiaryAiAction.EXPAND -> """
                扩写当前日记正文：保留原意和事实，不新增关键事件；补充自然细节、场景过渡和真实感受，让内容更完整、更有日记感。
                输出 content 必须是扩写后的完整正文。
            """.trimIndent()
            DiaryAiAction.SHORTEN -> """
                缩写当前日记正文：保留核心事件、情绪和重要想法，删除重复和松散表达，让文字更简洁清楚。
                输出 content 必须是缩写后的完整正文。
            """.trimIndent()
            DiaryAiAction.POLISH -> """
                润色当前日记正文：优化表达、语序和自然度，不改变事实，不改变用户原本语气，不写成作文或鸡汤。
                输出 content 必须是润色后的完整正文。
            """.trimIndent()
            DiaryAiAction.CONTINUE -> """
                续写当前日记正文：基于已有内容自然延展感受、思考或收束；不要突然加入新人物、新地点、新事件。
                输出 content 必须是包含原文和续写内容的完整正文。
            """.trimIndent()
            DiaryAiAction.GENERATE_TITLE -> """
                根据当前正文生成最适合的日记标题。标题要短、自然、有日记感，不要夸张，不要营销化。
                输出 title 必须是你认为最合适的一个标题；content 保持原正文不变。
            """.trimIndent()
            DiaryAiAction.SUMMARIZE_EMOTION -> """
                总结当前日记的主要情绪：提炼用户今天的情绪状态，选择最匹配的 mood，并生成一句克制、准确的 summary。
                不要做心理诊断，不要给治疗建议。content 保持原正文不变。
            """.trimIndent()
            DiaryAiAction.RECOMMEND_TAGS_GROUP -> """
                根据当前标题和正文推荐标签与分组。tags 给 2 到 5 个中文短标签，不带 #；group 给一个简短中文分组。
                优先从生活、学习、情绪、目标、旅行、读书、工作、关系、健康、灵感中选择 group。content 保持原正文不变。
            """.trimIndent()
        }

        return """
            请对当前日记执行一个明确的 AI 写作任务。

            【本次任务】
            ${action.displayName}

            【任务指令】
            $actionInstruction

            【当前日期】
            ${generationContext.dateText}

            【当前标题】
            ${generationContext.existingTitle.ifBlank { "无" }}

            【当前正文】
            ${generationContext.existingContent.ifBlank { "无" }}

            【当前心情】
            ${generationContext.currentMood.ifBlank { "neutral" }}

            【当前标签】
            $tagsText

            【当前分组】
            ${generationContext.currentGroup.ifBlank { "无" }}

            【通用要求】
            1. 只执行“${action.displayName}”这一项任务，不要擅自做其他处理。
            2. 不得编造用户没有提供的关键事实、人物、地点和结果。
            3. 正文必须保持第一人称私人日记语气。
            4. 不要写成作文、报告、公众号、鸡汤或心理咨询建议。
            5. mood 只能是 happy、calm、excited、sad、neutral 之一。
            6. tags 必须是 2 到 5 个中文短标签，不带 #，每个标签不超过 6 个字。
            7. group 必须是一个简短中文分组。
            8. 只返回严格 JSON，不要 Markdown，不要解释，不要代码块。

            返回格式：
            {
              "title": "标题；如果本任务不改标题则返回原标题",
              "content": "正文；如果本任务不改正文则返回原正文",
              "mood": "happy | calm | excited | sad | neutral",
              "tags": ["标签1", "标签2", "标签3"],
              "group": "分组",
              "summary": "一句话摘要",
              "writingStyle": "本次处理采用的方式说明"
            }
        """.trimIndent()
    }

    companion object {
        private val DIARY_SYSTEM_PROMPT = """
            你是“拾光札记”的专业中文日记写作助手，擅长根据用户的问答内容、已有正文、心情、标签、分组和写作模板，生成真实、自然、有个人感的中文日记。

            你的任务不是写作文，不是写鸡汤，也不是写营销文案，而是帮助用户把零散回答整理成一篇像用户本人写出来的日记。

            你必须遵守以下原则：

            1. 真实性优先
            - 只能基于用户提供的信息进行整理、融合和适度润色。
            - 不得编造用户没有提到的关键人物、地点、事件、关系、结果。
            - 如果用户回答很少，可以写得简洁，但不能为了丰富而虚构。

            2. 第一人称表达
            - 正文必须使用第一人称“我”。
            - 语气应像私人日记，而不是报告、公众号文章、作文或心理咨询记录。

            3. 自然克制
            - 语言自然、清楚、有生活感。
            - 可以适度加入细节连接和情绪过渡，但不要夸张抒情。
            - 不要使用过度华丽、空泛、鸡汤化的句子。

            4. 数据融合
            - 你需要综合当前日期、已有标题、已有正文、当前心情、当前标签、当前分组、写作模板、模板问题、用户回答、输出风格和长度偏好。
            - 你要将这些数据自然融合，而不是机械罗列问答。

            5. 保留用户意图
            - 如果用户已有正文，新生成内容应与已有正文自然衔接。
            - 如果已有正文较完整，可以进行整理、补充和结构优化。
            - 如果已有正文为空，则根据问答生成完整日记。
            - 不要改变用户原本表达的核心意思。

            6. 输出格式
            - 你必须只输出严格 JSON。
            - 不要输出 Markdown。
            - 不要输出解释。
            - 不要输出代码块。
            - 不要在 JSON 外添加任何文字。
        """.trimIndent()
    }

    private fun chatCompletion(
        config: AiPreferences.AiConfig,
        messages: List<ChatMessage>
    ): String {
        val endpoint = config.baseUrl.trimEnd('/') + "/chat/completions"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 60_000
            doInput = true
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer ${config.apiKey}")
        }

        val body = ChatCompletionRequest(
            model = config.model,
            messages = messages,
            temperature = 0.75,
            response_format = mapOf("type" to "json_object")
        )

        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(gson.toJson(body))
        }

        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val response = stream.bufferedReader(Charsets.UTF_8).use(BufferedReader::readText)
        connection.disconnect()

        if (status !in 200..299) {
            error("AI 请求失败：HTTP $status，$response")
        }

        val root = JsonParser.parseString(response).asJsonObject
        return root
            .getAsJsonArray("choices")
            .firstOrNull()
            ?.asJsonObject
            ?.getAsJsonObject("message")
            ?.get("content")
            ?.asString
            ?: error("AI 返回内容为空")
    }

    private fun parseGeneratedDiary(raw: String): GeneratedDiaryResult {
        val jsonText = raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val obj = JsonParser.parseString(jsonText).asJsonObject
        val tags = obj.getAsJsonArray("tags")?.mapNotNull { it.asString.trim().takeIf { tag -> tag.isNotBlank() } } ?: emptyList()
        return GeneratedDiaryResult(
            title = obj.get("title")?.asString.orEmpty(),
            content = obj.get("content")?.asString.orEmpty(),
            mood = obj.get("mood")?.asString?.takeIf { it in setOf("happy", "calm", "excited", "sad", "neutral") } ?: "neutral",
            tags = tags.take(5),
            group = obj.get("group")?.asString.orEmpty(),
            summary = obj.get("summary")?.asString.orEmpty(),
            writingStyle = obj.get("writingStyle")?.asString.orEmpty()
        )
    }

    private data class ChatCompletionRequest(
        val model: String,
        val messages: List<ChatMessage>,
        val temperature: Double,
        val response_format: Map<String, String>? = null
    )

    private data class ChatMessage(
        val role: String,
        val content: String
    )
}
