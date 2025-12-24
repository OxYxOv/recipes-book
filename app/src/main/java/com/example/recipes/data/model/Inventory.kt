package com.example.recipes.data.model

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

data class Item(
    val name: String,
    val weight: Double
)

class Inventory(private val maxWeight: Double = 100.0) {

    private val items: MutableMap<String, Item> = mutableMapOf()
    private val lock = ReentrantLock()

    fun addItem(item: Item): Boolean = lock.withLock {
        require(item.weight >= 0) { "Item weight cannot be negative" }

        val existing = items[item.name]
        val combinedWeight = (existing?.weight ?: 0.0) + item.weight
        val proposedTotal = currentWeightUnsafe() - (existing?.weight ?: 0.0) + combinedWeight

        if (proposedTotal > maxWeight) {
            return false
        }

        items[item.name] = Item(item.name, combinedWeight)
        return true
    }

    fun removeItem(name: String): Boolean = lock.withLock {
        items.remove(name) != null
    }

    fun getItems(): List<Item> = lock.withLock {
        items.values.map { it.copy() }
    }

    fun currentWeight(): Double = lock.withLock {
        currentWeightUnsafe()
    }

    fun findByName(query: String): List<Item> = lock.withLock {
        val normalized = query.lowercase()
        items.values
            .filter { it.name.lowercase().contains(normalized) }
            .map { it.copy() }
    }

    private fun currentWeightUnsafe(): Double {
        return items.values.sumOf { it.weight }
    }
}
