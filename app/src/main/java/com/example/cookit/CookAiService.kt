package com.example.cookit

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost

object CookAiService {

    private const val TOKEN = "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"

    // ✅ TRUE = мгновенные тестовые данные (для проверки UI)
    // ✅ FALSE = реальный запрос к AI
    private const val MOCK_MODE = false

    private val openAI = OpenAI(
        config = OpenAIConfig(
            token = TOKEN,
            host = OpenAIHost(baseUrl = "https://router.huggingface.co/v1/")
        )
    )

    private val SYSTEM_PROMPT = """
        Ты — кулинарный помощник. Пользователь даёт список ингредиентов.
        Предложи 4-5 блюд которые можно приготовить из этих ингредиентов (некоторые можно не использовать).
        
        ВАЖНО: Отвечай ТОЛЬКО валидным JSON без каких-либо пояснений, markdown-разметки или текста вне JSON.
        
        Формат ответа (строго):
        {
          "dishes": [
            {
              "name": "Название блюда",
              "emoji": "🍳",
              "short_desc": "Краткое описание в 1-2 предложения что за блюдо",
              "cook_time": "30 мин",
              "ingredients": [
                {"name": "Ингредиент", "amount": "200 г"},
                {"name": "Другой ингредиент", "amount": "3 шт"}
              ],
              "steps": [
                "Подробный шаг приготовления 1...",
                "Подробный шаг приготовления 2...",
                "Подробный шаг приготовления 3..."
              ]
            }
          ]
        }
        
        - emoji: подходящий эмодзи для блюда
        - short_desc: 1-2 предложения, аппетитное описание
        - cook_time: реалистичное время (например "20 мин", "1 час")
        - ingredients: точные граммовки или штуки
        - steps: 3-6 подробных шагов приготовления
        - Все на русском языке
        - ТОЛЬКО JSON, никакого другого текста
    """.trimIndent()

    suspend fun getDishes(ingredients: List<String>): String {
        if (MOCK_MODE) {
            kotlinx.coroutines.delay(1200) // имитируем загрузку
            return MOCK_JSON
        }

        val userMessage = "У меня есть: ${ingredients.joinToString(", ")}. Что можно приготовить?"

        val messages = listOf(
            ChatMessage(role = ChatRole.System, content = SYSTEM_PROMPT),
            ChatMessage(role = ChatRole.User, content = userMessage)
        )

        val request = ChatCompletionRequest(
            // 🚀 Быстрая модель — отвечает за 5-10 сек вместо таймаута
            model = ModelId("Qwen/Qwen2.5-72B-Instruct"),
            messages = messages,
            maxTokens = 2048
        )

        return openAI.chatCompletion(request).choices.first().message.content
            ?: throw Exception("Пустой ответ от сервера")
    }

    private val MOCK_JSON = """{"dishes":[{"name":"Классическая яичница с помидорами","emoji":"🍳","short_desc":"Сытный и быстрый завтрак за 10 минут. Яйца с сочными помидорами — всегда вкусно.","cook_time":"10 мин","ingredients":[{"name":"Яйца","amount":"3 шт"},{"name":"Помидоры","amount":"2 шт"},{"name":"Лук репчатый","amount":"0.5 шт"},{"name":"Масло растительное","amount":"1 ст. л."},{"name":"Соль и перец","amount":"по вкусу"}],"steps":["Нарежь помидоры кубиками, лук — полукольцами.","Разогрей сковороду с маслом, обжарь лук 2-3 минуты до прозрачности.","Добавь помидоры и обжаривай ещё 2 минуты.","Разбей яйца прямо на овощи, посоли и поперчи.","Накрой крышкой и готовь 3-4 минуты до желаемой прожарки."]},{"name":"Куриный суп с рисом","emoji":"🍲","short_desc":"Наваристый домашний суп на курином бульоне с рисом и овощами. Согревает и насыщает.","cook_time":"45 мин","ingredients":[{"name":"Куриное филе","amount":"400 г"},{"name":"Рис","amount":"80 г"},{"name":"Морковь","amount":"1 шт"},{"name":"Лук репчатый","amount":"1 шт"},{"name":"Картофель","amount":"2 шт"},{"name":"Соль, лавровый лист","amount":"по вкусу"}],"steps":["Залей курицу 1.5 л холодной воды, доведи до кипения, сними пену и вари 20 минут.","Достань курицу, нарежь на кусочки и верни в бульон.","Добавь нарезанный кубиками картофель и промытый рис.","Обжарь лук и морковь 5 минут и добавь в суп.","Вари ещё 15 минут до мягкости картофеля, посоли.","Дай настояться 5 минут под крышкой."]},{"name":"Макароны с сыром и чесноком","emoji":"🍝","short_desc":"Простое сытное блюдо в стиле итальянской пасты. Готовится за 20 минут.","cook_time":"20 мин","ingredients":[{"name":"Макароны","amount":"250 г"},{"name":"Сыр твёрдый","amount":"100 г"},{"name":"Чеснок","amount":"3 зубчика"},{"name":"Сливочное масло","amount":"40 г"},{"name":"Соль, чёрный перец","amount":"по вкусу"}],"steps":["Отвари макароны в подсоленной воде, слей воду, оставив 50 мл.","Растопи масло в сковороде, обжарь чеснок 1-2 минуты до аромата.","Добавь макароны и немного воды от варки, перемешай.","Натри сыр и всыпь в сковороду, быстро перемешивая.","Поперчи и подавай сразу."]},{"name":"Картофельные оладьи","emoji":"🥞","short_desc":"Хрустящие драники из картофеля — любимое блюдо с детства. Идеально со сметаной.","cook_time":"30 мин","ingredients":[{"name":"Картофель","amount":"500 г"},{"name":"Яйцо","amount":"1 шт"},{"name":"Лук репчатый","amount":"1 шт"},{"name":"Мука","amount":"2 ст. л."},{"name":"Масло растительное","amount":"3 ст. л."},{"name":"Соль","amount":"1 ч. л."}],"steps":["Натри картофель и лук на крупной тёрке, отожми лишний сок.","Добавь яйцо, муку и соль, перемешай.","Разогрей масло на сковороде на среднем огне.","Выкладывай ложкой, обжаривай по 3-4 минуты с каждой стороны.","Подавай горячими со сметаной."]},{"name":"Омлет с сыром","emoji":"🧀","short_desc":"Воздушный французский омлет с расплавленным сыром внутри. Нежный и аппетитный.","cook_time":"8 мин","ingredients":[{"name":"Яйца","amount":"3 шт"},{"name":"Молоко","amount":"3 ст. л."},{"name":"Сыр твёрдый","amount":"50 г"},{"name":"Сливочное масло","amount":"15 г"},{"name":"Соль","amount":"щепотка"}],"steps":["Взбей яйца с молоком и щепоткой соли.","Натри сыр на мелкой тёрке.","Растопи масло на сковороде, влей яичную смесь.","Когда края начнут схватываться, посыпь половину омлета сыром.","Сложи пополам и держи ещё 1 минуту. Подавай сразу."]}]}"""
}