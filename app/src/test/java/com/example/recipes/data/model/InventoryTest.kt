package com.example.recipes.data.model

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class InventoryTest {

    private lateinit var inventory: Inventory

    @Before
    fun setUp() {
        inventory = Inventory(maxWeight = 100.0)
    }

    @Test
    fun `adding item over max weight is rejected`() {
        assertTrue(inventory.addItem(Item("Stone", 90.0)))
        assertFalse(inventory.addItem(Item("Heavy Rock", 15.0)))
        assertEquals(90.0, inventory.currentWeight(), 0.0)
        assertEquals(1, inventory.getItems().size)
    }

    @Test
    fun `duplicate items are merged by name`() {
        assertTrue(inventory.addItem(Item("Apple", 10.0)))
        assertTrue(inventory.addItem(Item("Apple", 5.5)))

        val items = inventory.getItems()
        assertEquals(1, items.size)
        assertEquals(15.5, items.first().weight, 0.0)
    }

    @Test
    fun `find by name returns items containing substring case insensitive`() {
        inventory.addItem(Item("Apple Pie", 2.0))
        inventory.addItem(Item("Banana Bread", 3.0))
        inventory.addItem(Item("Grapes", 1.0))

        val result = inventory.findByName("ap")
        val names = result.map { it.name }.toSet()

        assertEquals(setOf("Apple Pie", "Grapes"), names)
    }

    @Test
    fun `returned items do not expose internal collection`() {
        inventory.addItem(Item("Milk", 2.0))

        val snapshot = inventory.getItems().toMutableList()
        snapshot.add(Item("Hacked", 1.0))

        assertEquals(1, inventory.getItems().size)
        assertEquals(2.0, inventory.currentWeight(), 0.0)
    }

    @Test
    fun `inventory operations are thread safe`() {
        val latch = CountDownLatch(10)
        val executor = Executors.newFixedThreadPool(5)

        try {
            repeat(10) {
                executor.execute {
                    inventory.addItem(Item("Coin", 2.0))
                    latch.countDown()
                }
            }

            assertTrue(latch.await(2, TimeUnit.SECONDS))
            executor.shutdown()

            val items = inventory.getItems()
            assertEquals(1, items.size)
            assertEquals(20.0, items.first().weight, 0.0)
        } finally {
            executor.shutdownNow()
        }
    }
}
