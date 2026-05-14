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

    // 1. Используем прямой путь к модели (Native API)
    // API Ключ передается в самом URL параметром ?key=
    private const val TOKEN = "AIzaSyCPF6mDYsWOgYclLZhvIOAKqTqS8ucjRmc"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$TOKEN"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(400, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val SYSTEM_PROMPT = """
    You are a cooking assistant. Suggest 1 to 10 real dishes from ingredients.
    IMPORTANT: Reply ONLY with valid JSON.
    Format:
    {
      "dishes": [
        {
          "name": "Название",
          "emoji": "🍳",
          "short_desc": "Описание",
          "cook_time": "30 мин",
          "ingredients": [{"name": "Ингредиент", "amount": "100 г"}],
          "steps": ["Шаг 1", "Шаг 2"]
        }
      ]
    }
    Language: Russian.
""".trimIndent()

    suspend fun getDishes(ingredients: List<String>): String = withContext(Dispatchers.IO) {
        if (MOCK_MODE) {
            kotlinx.coroutines.delay(1200)
            return@withContext MOCK_JSON
        }

        val userMessage = "Ингредиенты: ${ingredients.joinToString(", ")}. Что приготовить? Ответь только JSON на русском."

        // 2. Структура тела запроса для Родного API Gemini (отличается от OpenAI)
        val bodyJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            // Соединяем системный промпт и сообщение пользователя
                            put("text", "$SYSTEM_PROMPT\n\nUser request: $userMessage")
                        })
                    })
                })
            })
            // Дополнительно просим модель выдавать чистый JSON (JSON Mode)
            put("generationConfig", JSONObject().apply {
                put("response_mime_type", "application/json")
            })
        }.toString()

        val request = Request.Builder()
            .url(BASE_URL)
            .addHeader("Content-Type", "application/json")
            .post(bodyJson.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Пустой ответ")

        if (!response.isSuccessful) {
            throw Exception("Ошибка ${response.code}: $responseBody")
        }

        // 3. Парсинг ответа (у Gemini другая вложенность)
        val json = JSONObject(responseBody)
        var content = json
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")

        // Очистка от лишних символов (на всякий случай)
        val start = content.indexOf('{')
        val end = content.lastIndexOf('}')
        if (start != -1 && end != -1) {
            content = content.substring(start, end + 1)
        }

        content
    }

    private val MOCK_JSON = """ { "dishes": [] } """ // (оставил для примера)
}