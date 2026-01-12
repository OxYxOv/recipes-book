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
    version = 4,
    exportSchema = false
)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: RecipeDatabase? = null
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val existingColumns = mutableSetOf<String>()
                database.query("PRAGMA table_info(`recipes`)").use { cursor ->
                    val nameIndex = cursor.getColumnIndexOrThrow("name")
                    while (cursor.moveToNext()) {
                        existingColumns.add(cursor.getString(nameIndex))
                    }
                }

                if ("imageUrl" !in existingColumns) {
                    database.execSQL("ALTER TABLE recipes ADD COLUMN imageUrl TEXT")
                }
                if ("isFavorite" !in existingColumns) {
                    database.execSQL("ALTER TABLE recipes ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                }
                if ("isLocal" !in existingColumns) {
                    database.execSQL("ALTER TABLE recipes ADD COLUMN isLocal INTEGER NOT NULL DEFAULT 1")
                }
                if ("ownerId" !in existingColumns) {
                    database.execSQL("ALTER TABLE recipes ADD COLUMN ownerId TEXT")
                }
                if ("remoteId" !in existingColumns) {
                    database.execSQL("ALTER TABLE recipes ADD COLUMN remoteId TEXT")
                }

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS favorite_recipes (
                        userId TEXT NOT NULL,
                        recipeId INTEGER NOT NULL,
                        PRIMARY KEY(userId, recipeId),
                        FOREIGN KEY(recipeId) REFERENCES recipes(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE `hidden_recipes` (
                        `userId` TEXT NOT NULL,
                        `recipeId` INTEGER NOT NULL,
                        PRIMARY KEY(`userId`, `recipeId`)
                    )
                    """.trimIndent()
                )
            }
        }
        // Normalize the hidden_recipes table created in v3 to include the foreign key constraint
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE hidden_recipes_new (
                        userId TEXT NOT NULL,
                        recipeId INTEGER NOT NULL,
                        PRIMARY KEY(userId, recipeId),
                        FOREIGN KEY(recipeId) REFERENCES recipes(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT OR IGNORE INTO hidden_recipes_new(userId, recipeId)
                    SELECT userId, recipeId FROM hidden_recipes
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE hidden_recipes")
                database.execSQL("ALTER TABLE hidden_recipes_new RENAME TO hidden_recipes")
            }
        }

        fun getDatabase(context: Context): RecipeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    "recipe_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
