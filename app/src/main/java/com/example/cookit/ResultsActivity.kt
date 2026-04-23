package com.example.cookit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class ResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarResults)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val layoutLoading = findViewById<LinearLayout>(R.id.layoutLoading)
        val layoutError = findViewById<LinearLayout>(R.id.layoutError)
        val tvError = findViewById<TextView>(R.id.tvError)
        val containerDishes = findViewById<LinearLayout>(R.id.containerDishes)

        val ingredients = intent.getStringArrayListExtra("ingredients") ?: arrayListOf()

        // Показываем загрузку
        layoutLoading.visibility = View.VISIBLE
        layoutError.visibility = View.GONE
        containerDishes.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val raw = CookAiService.getDishes(ingredients)
                val dishes = parseDishesJson(raw)

                layoutLoading.visibility = View.GONE
                containerDishes.visibility = View.VISIBLE

                dishes.forEachIndexed { index, dish ->
                    val cardView = layoutInflater.inflate(
                        R.layout.item_dish_card, containerDishes, false
                    )
                    val accentBar = cardView.findViewById<View>(R.id.accentBar)
                    val tvEmoji = cardView.findViewById<TextView>(R.id.tvDishEmoji)
                    val tvName = cardView.findViewById<TextView>(R.id.tvDishName)
                    val tvDesc = cardView.findViewById<TextView>(R.id.tvDishDesc)
                    val tvTime = cardView.findViewById<TextView>(R.id.tvCookTime)

                    // Чередуем зелёный и красный акцент
                    val accentColor = if (index % 2 == 0) {
                        getColor(R.color.green_primary)
                    } else {
                        getColor(R.color.red_accent)
                    }
                    accentBar.backgroundTintList = android.content.res.ColorStateList.valueOf(accentColor)

                    tvEmoji.text = dish.emoji
                    tvName.text = dish.name
                    tvDesc.text = dish.shortDesc
                    tvTime.text = "⏱ ${dish.cookTime}"

                    cardView.setOnClickListener {
                        val intent = Intent(this@ResultsActivity, RecipeActivity::class.java)
                        intent.putExtra("dish_json", dish.toJson())
                        startActivity(intent)
                    }

                    containerDishes.addView(cardView)
                }

            } catch (e: Exception) {
                layoutLoading.visibility = View.GONE
                layoutError.visibility = View.VISIBLE
                tvError.text = "Не удалось получить рецепты 😕\n${e.message}"
            }
        }
    }
}