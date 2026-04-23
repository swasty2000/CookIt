package com.example.cookit

import org.json.JSONArray
import org.json.JSONObject

data class Dish(
    val name: String,
    val emoji: String,
    val shortDesc: String,
    val cookTime: String,
    val ingredients: List<Ingredient>,
    val steps: List<String>
) {
    fun toJson(): String {
        val obj = JSONObject()
        obj.put("name", name)
        obj.put("emoji", emoji)
        obj.put("short_desc", shortDesc)
        obj.put("cook_time", cookTime)

        val ingArr = JSONArray()
        ingredients.forEach {
            val ing = JSONObject()
            ing.put("name", it.name)
            ing.put("amount", it.amount)
            ingArr.put(ing)
        }
        obj.put("ingredients", ingArr)

        val stepsArr = JSONArray()
        steps.forEach { stepsArr.put(it) }
        obj.put("steps", stepsArr)

        return obj.toString()
    }

    companion object {
        fun fromJson(json: String): Dish {
            val obj = JSONObject(json)
            val ingList = mutableListOf<Ingredient>()
            val ingArr = obj.getJSONArray("ingredients")
            for (i in 0 until ingArr.length()) {
                val ing = ingArr.getJSONObject(i)
                ingList.add(Ingredient(ing.getString("name"), ing.getString("amount")))
            }
            val stepsList = mutableListOf<String>()
            val stepsArr = obj.getJSONArray("steps")
            for (i in 0 until stepsArr.length()) {
                stepsList.add(stepsArr.getString(i))
            }
            return Dish(
                name = obj.getString("name"),
                emoji = obj.getString("emoji"),
                shortDesc = obj.getString("short_desc"),
                cookTime = obj.getString("cook_time"),
                ingredients = ingList,
                steps = stepsList
            )
        }
    }
}

data class Ingredient(
    val name: String,
    val amount: String
)

fun parseDishesJson(raw: String): List<Dish> {
    // Вырезаем JSON из возможного мусора вокруг (на случай если модель добавит текст)
    val jsonStart = raw.indexOf('{')
    val jsonEnd = raw.lastIndexOf('}')
    if (jsonStart == -1 || jsonEnd == -1) throw Exception("Неверный формат ответа")

    val clean = raw.substring(jsonStart, jsonEnd + 1)
    val root = JSONObject(clean)
    val arr = root.getJSONArray("dishes")
    val dishes = mutableListOf<Dish>()

    for (i in 0 until arr.length()) {
        val obj = arr.getJSONObject(i)
        val ingList = mutableListOf<Ingredient>()
        val ingArr = obj.getJSONArray("ingredients")
        for (j in 0 until ingArr.length()) {
            val ing = ingArr.getJSONObject(j)
            ingList.add(Ingredient(ing.getString("name"), ing.getString("amount")))
        }
        val stepsList = mutableListOf<String>()
        val stepsArr = obj.getJSONArray("steps")
        for (j in 0 until stepsArr.length()) {
            stepsList.add(stepsArr.getString(j))
        }
        dishes.add(
            Dish(
                name = obj.getString("name"),
                emoji = obj.optString("emoji", "🍽️"),
                shortDesc = obj.optString("short_desc", ""),
                cookTime = obj.optString("cook_time", ""),
                ingredients = ingList,
                steps = stepsList
            )
        )
    }
    return dishes
}