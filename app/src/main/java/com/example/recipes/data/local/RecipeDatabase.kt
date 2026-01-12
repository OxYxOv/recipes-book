package com.example.recipes.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.recipes.data.model.FavoriteRecipe
import com.example.recipes.data.model.HiddenRecipe
import com.example.recipes.data.model.Recipe

@Database(
    entities = [Recipe::class, FavoriteRecipe::class, HiddenRecipe::class],
    version = 3,
    exportSchema = false
)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: RecipeDatabase? = null
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `hidden_recipes` (
                        `userId` TEXT NOT NULL,
                        `recipeId` INTEGER NOT NULL,
                        PRIMARY KEY(`userId`, `recipeId`)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): RecipeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    "recipe_database"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
