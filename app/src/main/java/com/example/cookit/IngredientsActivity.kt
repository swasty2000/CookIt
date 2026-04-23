package com.example.cookit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

class IngredientsActivity : AppCompatActivity() {

    private val selectedIngredients = mutableListOf<String>()

    private val popularIngredients = listOf(
        "🍗 Курица", "🍅 Помидоры", "🧅 Лук", "🧄 Чеснок",
        "🥔 Картофель", "🥚 Яйца", "🧀 Сыр", "🍝 Макароны",
        "🍚 Рис", "🥒 Огурец", "🥦 Брокколи", "🥕 Морковь",
        "🥩 Говядина", "🐟 Рыба", "🥜 Фасоль", "🍋 Лимон"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingredients)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val etIngredient = findViewById<TextInputEditText>(R.id.etIngredient)
        val btnAdd = findViewById<MaterialButton>(R.id.btnAdd)
        val chipGroupSuggestions = findViewById<ChipGroup>(R.id.chipGroupSuggestions)
        val chipGroupSelected = findViewById<ChipGroup>(R.id.chipGroupSelected)
        val tvSelectedTitle = findViewById<TextView>(R.id.tvSelectedTitle)
        val btnFindDishes = findViewById<MaterialButton>(R.id.btnFindDishes)

        // Популярные ингредиенты
        popularIngredients.forEach { name ->
            val chip = Chip(this).apply {
                text = name
                isCheckable = true
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                    getColor(R.color.surface_card)
                )
                setTextColor(getColor(R.color.green_accent))
                chipStrokeWidth = 1.5f
                chipStrokeColor = android.content.res.ColorStateList.valueOf(
                    getColor(R.color.green_dim)
                )
            }
            chip.setOnCheckedChangeListener { _, checked ->
                val cleanName = name.substringAfter(" ")
                if (checked) {
                    chip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                        getColor(R.color.green_dim)
                    )
                    chip.setTextColor(getColor(R.color.white))
                    addIngredient(cleanName, chipGroupSelected, tvSelectedTitle)
                } else {
                    chip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                        getColor(R.color.surface_card)
                    )
                    chip.setTextColor(getColor(R.color.green_accent))
                    removeIngredient(cleanName, chipGroupSelected, tvSelectedTitle)
                }
            }
            chipGroupSuggestions.addView(chip)
        }

        btnAdd.setOnClickListener {
            addFromInput(etIngredient, chipGroupSelected, tvSelectedTitle)
        }

        etIngredient.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addFromInput(etIngredient, chipGroupSelected, tvSelectedTitle)
                true
            } else false
        }

        btnFindDishes.setOnClickListener {
            if (selectedIngredients.isEmpty()) {
                Toast.makeText(this, "Добавь хотя бы один ингредиент 🥕", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, ResultsActivity::class.java)
                intent.putStringArrayListExtra("ingredients", ArrayList(selectedIngredients))
                startActivity(intent)
            }
        }
    }

    private fun addFromInput(et: TextInputEditText, group: ChipGroup, title: TextView) {
        val name = et.text?.toString()?.trim() ?: return
        if (name.isNotEmpty()) {
            addIngredient(name, group, title)
            et.text?.clear()
        }
    }

    private fun addIngredient(name: String, group: ChipGroup, title: TextView) {
        if (selectedIngredients.contains(name)) return
        selectedIngredients.add(name)

        val chip = Chip(this).apply {
            text = name
            isCloseIconVisible = true
            chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                getColor(R.color.red_dim)
            )
            setTextColor(getColor(R.color.text_light))
            chipStrokeWidth = 1f
            chipStrokeColor = android.content.res.ColorStateList.valueOf(
                getColor(R.color.red_accent)
            )
            closeIconTint = android.content.res.ColorStateList.valueOf(
                getColor(R.color.text_light)
            )
        }
        chip.setOnCloseIconClickListener {
            group.removeView(chip)
            selectedIngredients.remove(name)
            if (selectedIngredients.isEmpty()) title.visibility = View.GONE
        }
        group.addView(chip)
        title.visibility = View.VISIBLE
    }

    private fun removeIngredient(name: String, group: ChipGroup, title: TextView) {
        selectedIngredients.remove(name)
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as? Chip
            if (chip?.text == name) {
                group.removeView(chip)
                break
            }
        }
        if (selectedIngredients.isEmpty()) title.visibility = View.GONE
    }
}