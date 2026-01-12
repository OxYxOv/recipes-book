package com.example.recipes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.recipes.data.local.RecipeDatabase
import com.example.recipes.data.local.UserPreferencesManager
import com.example.recipes.data.model.Recipe
import com.example.recipes.ui.navigation.AppNavigation
import com.example.recipes.ui.theme.RecipesBookTheme
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val preferencesManager by lazy { UserPreferencesManager(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Add sample recipes on first launch
        addSampleRecipes()

        setContent {
            RecipesBookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    private fun addSampleRecipes() {
        lifecycleScope.launch {
            val db = RecipeDatabase.getDatabase(applicationContext)
            val dao = db.recipeDao()
            val userId = preferencesManager.userEmail.firstOrNull()
            // Check if database is empty
            val hasRecipes = dao.getAllRecipes(userId).first().isNotEmpty()

            if (!hasRecipes) {
                // Add sample recipes
                val sampleRecipes = listOf(
                    Recipe(
                        name = "Омлет с овощами",
                        description = "Легкий и питательный завтрак с овощами",
                        ingredients = "3 яйца\n1 помидор\n1 болгарский перец\nСоль, перец по вкусу\n2 столовые ложки молока",
                        instructions = "1. Взбейте яйца с молоком\n2. Нарежьте овощи мелкими кубиками\n3. Разогрейте сковороду с маслом\n4. Вылейте яйца и добавьте овощи\n5. Готовьте на среднем огне 5-7 минут",
                        category = "breakfast",
                        cookingTime = 15,
                        difficulty = "easy",
                        servings = 2,
                        imageUrl = "https://images.unsplash.com/photo-1540189549336-e6e99c3679fe?auto=format&fit=crop&w=1200&q=60"
                    ),
                    Recipe(
                        name = "Борщ",
                        description = "Классический украинский суп",
                        ingredients = "500г говядины\n2 свеклы\n3 картофеля\n1 морковь\n1 луковица\n1/2 капусты\nТоматная паста\nСоль, перец, лавровый лист",
                        instructions = "1. Сварите бульон из мяса (1,5 часа)\n2. Нарежьте овощи\n3. Обжарьте лук, морковь и свеклу\n4. Добавьте в бульон картофель\n5. Через 10 минут добавьте капусту и зажарку\n6. Варите 20 минут\n7. Добавьте специи",
                        category = "lunch",
                        cookingTime = 120,
                        difficulty = "medium",
                        servings = 6,
                        imageUrl = "https://images.unsplash.com/photo-1604908176997-1251882d99f1?auto=format&fit=crop&w=1200&q=60"
                    ),
                    Recipe(
                        name = "Куриные котлеты",
                        description = "Сочные домашние котлеты из курицы",
                        ingredients = "600г куриного фарша\n1 луковица\n2 зубчика чеснока\n1 яйцо\n3 столовые ложки муки\nСоль, перец, специи",
                        instructions = "1. Смешайте фарш с измельченным луком и чесноком\n2. Добавьте яйцо, муку и специи\n3. Сформируйте котлеты\n4. Обжарьте на сковороде по 5 минут с каждой стороны\n5. Доведите до готовности в духовке 15 минут при 180°C",
                        category = "dinner",
                        cookingTime = 40,
                        difficulty = "easy",
                        servings = 4,
                        imageUrl = "https://images.unsplash.com/photo-1604908177035-0ac1e435cd1d?auto=format&fit=crop&w=1200&q=60"
                    ),
                    Recipe(
                        name = "Шоколадный кекс",
                        description = "Нежный шоколадный десерт",
                        ingredients = "200г муки\n200г сахара\n100г какао\n2 яйца\n150мл молока\n100мл растительного масла\n1 чайная ложка разрыхлителя",
                        instructions = "1. Смешайте сухие ингредиенты\n2. Взбейте яйца с сахаром\n3. Добавьте молоко и масло\n4. Соедините с сухой смесью\n5. Выпекайте при 180°C 35-40 минут",
                        category = "dessert",
                        cookingTime = 50,
                        difficulty = "easy",
                        servings = 8,
                        imageUrl = "https://images.unsplash.com/photo-1599785209796-86e3b8329fcb?auto=format&fit=crop&w=1200&q=60"
                    ),
                    Recipe(
                        name = "Цезарь салат",
                        description = "Популярный салат с курицей",
                        ingredients = "300г куриной грудки\n1 салат романо\n100г пармезана\nКрутоны\nСоус цезарь\nОливковое масло",
                        instructions = "1. Обжарьте куриную грудку\n2. Нарежьте салат\n3. Натрите пармезан\n4. Смешайте все ингредиенты\n5. Добавьте соус и крутоны",
                        category = "snack",
                        cookingTime = 20,
                        difficulty = "easy",
                        servings = 2,
                        imageUrl = "https://images.unsplash.com/photo-1551183053-bf91a1d81141?auto=format&fit=crop&w=1200&q=60"
                    ),
                    Recipe(
                        name = "Лазанья",
                        description = "Итальянская запеканка с мясом и сыром",
                        ingredients = "500г фарша\n12 листов лазаньи\n400г томатного соуса\n500мл бешамеля\n200г моцареллы\n100г пармезана\nЛук, чеснок, специи",
                        instructions = "1. Обжарьте фарш с луком и чесноком\n2. Добавьте томатный соус, тушите 15 минут\n3. Выложите слоями: соус, листы лазаньи, бешамель\n4. Повторите слои\n5. Посыпьте сыром\n6. Запекайте при 180°C 40 минут",
                        category = "dinner",
                        cookingTime = 90,
                        difficulty = "hard",
                        servings = 6,
                        imageUrl = "https://images.unsplash.com/photo-1612874472278-5c1f9f0d4d5d?auto=format&fit=crop&w=1200&q=60"
                    )
                )

                sampleRecipes.forEach { recipe ->
                    dao.insertRecipe(recipe)
                }
            }
        }
    }
}
