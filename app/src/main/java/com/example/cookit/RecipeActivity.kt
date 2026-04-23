package com.example.cookit

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar

class RecipeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarRecipe)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val dishJson = intent.getStringExtra("dish_json") ?: run { finish(); return }
        val dish = Dish.fromJson(dishJson)

        // Заполняем шапку
        findViewById<TextView>(R.id.tvRecipeEmoji).text = dish.emoji
        findViewById<TextView>(R.id.tvRecipeName).text = dish.name
        findViewById<TextView>(R.id.tvRecipeCookTime).text = "⏱ ${dish.cookTime}"
        toolbar.title = dish.name

        // Заполняем список ингредиентов
        val containerIngredients = findViewById<LinearLayout>(R.id.containerIngredients)
        dish.ingredients.forEachIndexed { index, ingredient ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.bottomMargin = if (index < dish.ingredients.lastIndex) dpToPx(10) else 0
                layoutParams = lp
            }

            // Точка-буллет
            val bullet = TextView(this).apply {
                text = "●"
                textSize = 8f
                setTextColor(ContextCompat.getColor(this@RecipeActivity, R.color.green_primary))
                val lp = LinearLayout.LayoutParams(dpToPx(20), LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams = lp
                gravity = Gravity.CENTER_VERTICAL
            }

            // Название ингредиента
            val tvName = TextView(this).apply {
                text = ingredient.name
                textSize = 15f
                setTextColor(ContextCompat.getColor(this@RecipeActivity, R.color.text_light))
                val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                layoutParams = lp
            }

            // Количество
            val tvAmount = TextView(this).apply {
                text = ingredient.amount
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@RecipeActivity, R.color.green_accent))
            }

            row.addView(bullet)
            row.addView(tvName)
            row.addView(tvAmount)
            containerIngredients.addView(row)

            // Разделитель (кроме последнего)
            if (index < dish.ingredients.lastIndex) {
                val divider = android.view.View(this).apply {
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)
                    )
                    lp.topMargin = dpToPx(10)
                    layoutParams = lp
                    setBackgroundColor(ContextCompat.getColor(this@RecipeActivity, R.color.divider))
                }
                containerIngredients.addView(divider)
            }
        }

        // Заполняем шаги приготовления
        val containerSteps = findViewById<LinearLayout>(R.id.containerSteps)
        dish.steps.forEachIndexed { index, step ->
            val stepRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.bottomMargin = if (index < dish.steps.lastIndex) dpToPx(16) else 0
                layoutParams = lp
            }

            // Номер шага в кружке
            val tvNumber = TextView(this).apply {
                text = "${index + 1}"
                textSize = 13f
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(this@RecipeActivity, R.color.white))
                setBackgroundColor(ContextCompat.getColor(this@RecipeActivity, R.color.red_accent))
                val lp = LinearLayout.LayoutParams(dpToPx(28), dpToPx(28))
                lp.marginEnd = dpToPx(14)
                lp.topMargin = dpToPx(2)
                layoutParams = lp
                // Круглый фон через background tint
                background = ContextCompat.getDrawable(this@RecipeActivity, R.drawable.bg_step_number)
            }

            // Текст шага
            val tvStep = TextView(this).apply {
                text = step
                textSize = 15f
                setTextColor(ContextCompat.getColor(this@RecipeActivity, R.color.text_light))
                lineHeight = (15 * 1.5 * resources.displayMetrics.scaledDensity).toInt()
                val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                layoutParams = lp
            }

            stepRow.addView(tvNumber)
            stepRow.addView(tvStep)
            containerSteps.addView(stepRow)
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()
}
