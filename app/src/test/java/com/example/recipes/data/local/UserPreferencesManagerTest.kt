package com.example.recipes.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class UserPreferencesManagerTest {

    private lateinit var context: Context
    private lateinit var preferencesManager: UserPreferencesManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        preferencesManager = UserPreferencesManager(context)
    }

    @Test
    fun `saveUserData should store all user data`() = runTest {
        // Given
        val username = "TestUser"
        val email = "test@example.com"
        val token = "test_token_123"

        // When
        preferencesManager.saveUserData(username, email, token)

        // Then
        val savedToken = preferencesManager.userToken.first()
        val savedName = preferencesManager.userName.first()
        val isLoggedIn = preferencesManager.isLoggedIn.first()

        assertEquals(token, savedToken)
        assertEquals(username, savedName)
        assertTrue(isLoggedIn)
    }

    @Test
    fun `clearUserData should remove all data`() = runTest {
        // Given - save some data first
        preferencesManager.saveUserData("TestUser", "test@example.com", "token")
        
        // Verify data is saved
        assertTrue(preferencesManager.isLoggedIn.first())

        // When
        preferencesManager.clearUserData()

        // Then
        val token = preferencesManager.userToken.first()
        val userName = preferencesManager.userName.first()
        val isLoggedIn = preferencesManager.isLoggedIn.first()

        assertNull(token)
        assertNull(userName)
        assertFalse(isLoggedIn)
    }

    @Test
    fun `isLoggedIn should be false by default`() = runTest {
        // When
        val isLoggedIn = preferencesManager.isLoggedIn.first()

        // Then
        assertFalse(isLoggedIn)
    }

    @Test
    fun `userToken should be null by default`() = runTest {
        // When
        val token = preferencesManager.userToken.first()

        // Then
        assertNull(token)
    }

    @Test
    fun `userName should be null by default`() = runTest {
        // When
        val userName = preferencesManager.userName.first()

        // Then
        assertNull(userName)
    }

    @Test
    fun `multiple saves should overwrite previous data`() = runTest {
        // Given
        preferencesManager.saveUserData("User1", "user1@example.com", "token1")

        // When
        preferencesManager.saveUserData("User2", "user2@example.com", "token2")

        // Then
        val savedToken = preferencesManager.userToken.first()
        val savedName = preferencesManager.userName.first()

        assertEquals("token2", savedToken)
        assertEquals("User2", savedName)
    }
}
