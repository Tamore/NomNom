package com.nomnom.data.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ShoppingItem(
    val id: String,
    val name: String,
    val category: String,
    val checked: Boolean = false
)

class ShoppingViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val items: StateFlow<List<ShoppingItem>> = _items

    fun addIngredients(ingredients: List<String>, recipeTitle: String = "") {
        val new = ingredients.map { ing ->
            ShoppingItem(
                id       = "${recipeTitle}_${ing}_${System.nanoTime()}",
                name     = ing,
                category = categorise(ing)
            )
        }
        // Avoid exact duplicates (same text already in list)
        val existing = _items.value.map { it.name.lowercase() }.toSet()
        val toAdd    = new.filter { it.name.lowercase() !in existing }
        _items.value = _items.value + toAdd
    }

    fun addItem(name: String, category: String? = null) {
        if (name.isBlank()) return
        val cat = category ?: categorise(name)
        val newItem = ShoppingItem(
            id = "manual_${System.nanoTime()}",
            name = name,
            category = cat
        )
        _items.value = _items.value + newItem
    }

    fun toggleItem(id: String) {
        _items.value = _items.value.map {
            if (it.id == id) it.copy(checked = !it.checked) else it
        }
    }

    fun clearChecked() {
        _items.value = _items.value.filter { !it.checked }
    }

    fun clearAll() {
        _items.value = emptyList()
    }

    private fun categorise(ingredient: String): String {
        val lower = ingredient.lowercase()
        return when {
            lower.containsAny("lettuce","spinach","kale","cabbage","broccoli","carrot","onion","garlic",
                               "tomato","pepper","celery","zucchini","cucumber","potato","mushroom",
                               "herb","parsley","cilantro","basil","mint","lemon","lime","apple","banana") -> "🥦 Produce"
            lower.containsAny("milk","cream","butter","cheese","yogurt","egg","sour cream") -> "🥛 Dairy & Eggs"
            lower.containsAny("chicken","beef","pork","lamb","turkey","fish","salmon","tuna",
                               "shrimp","bacon","sausage","meat") -> "🥩 Meat & Fish"
            lower.containsAny("flour","sugar","salt","pepper","oil","vinegar","baking","soy sauce",
                               "stock","broth","rice","pasta","noodle","bread","oat","honey","spice",
                               "cumin","paprika","cinnamon","oregano") -> "🫙 Pantry"
            lower.containsAny("ice cream","frozen","pea") -> "🧊 Frozen"
            else -> "🛒 Other"
        }
    }

    private fun String.containsAny(vararg terms: String) = terms.any { this.contains(it) }
}
