package com.example.cookit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object CookAiService {

    private const val MOCK_MODE = false

    private const val TOKEN = "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"

    // ✅ Правильный URL — как в официальном примере HuggingFace
    private const val BASE_URL = "https://router.huggingface.co/v1/chat/completions"

    // ✅ Провайдер :hf-inference обязателен в названии модели
    private const val MODEL = "meta-llama/Llama-3.1-8B-Instruct:novita"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val SYSTEM_PROMPT = """
    You are a cooking assistant. The user gives you a list of ingredients.
    Suggest 4-5 dishes that can be made from these ingredients.
    IMPORTANT: Reply ONLY with valid JSON, no explanations or markdown.
    Response format:
    {
      "dishes": [
        {
          "name": "Dish name in Russian",
          "emoji": "🍳",
          "short_desc": "Short description in Russian",
          "cook_time": "30 мин",
          "ingredients": [{"name": "Ingredient in Russian", "amount": "100 г"}],
          "steps": ["Write as many steps as needed to fully describe the recipe, do not limit yourself"]
        }
      ]
    }
    All text must be in Russian language.
    The "steps" array must contain ALL steps needed to cook the dish — typically 4-8 steps.
    Return ONLY the JSON object.
""".trimIndent()
    suspend fun getDishes(ingredients: List<String>): String = withContext(Dispatchers.IO) {
        if (MOCK_MODE) {
            kotlinx.coroutines.delay(1200)
            return@withContext MOCK_JSON
        }

        val userMessage = "Ingredients I have: ${ingredients.joinToString(", ")}. What can I cook? Reply in Russian with JSON only."

        // Тело запроса — модель передаётся В ТЕЛЕ, URL один для всех
        val body = JSONObject().apply {
            put("model", MODEL)
            put("max_tokens", 2048)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", SYSTEM_PROMPT)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userMessage)
                })
            })
        }.toString()

        val request = Request.Builder()
            .url(BASE_URL)
            .addHeader("Authorization", "Bearer $TOKEN")
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Пустой ответ")

        if (!response.isSuccessful) {
            throw Exception("Ошибка ${response.code}: $responseBody")
        }

        val json = JSONObject(responseBody)
        var content = json
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")

        // Вырезаем JSON если модель добавила текст вокруг
        val start = content.indexOf('{')
        val end = content.lastIndexOf('}')
        if (start != -1 && end != -1) {
            content = content.substring(start, end + 1)
        }

        content
    }

    private val MOCK_JSON = """
        {
          "dishes": [
            {
              "name": "Классическая яичница с помидорами",
              "emoji": "🍳",
              "short_desc": "Сытный и быстрый завтрак за 10 минут.",
              "cook_time": "10 мин",
              "ingredients": [
                {"name": "Яйца", "amount": "3 шт"},
                {"name": "Помидоры", "amount": "2 шт"},
                {"name": "Масло растительное", "amount": "1 ст. л."},
                {"name": "Соль и перец", "amount": "по вкусу"}
              ],
              "steps": [
                "Нарежь помидоры кубиками.",
                "Разогрей сковороду с маслом, обжарь помидоры 2 минуты.",
                "Разбей яйца, посоли и поперчи.",
                "Накрой крышкой и готовь 3-4 минуты."
              ]
            }
          ]
        }
    """.trimIndent()
}