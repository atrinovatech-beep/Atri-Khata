package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.AccountType
import com.example.data.auth.AtriUserSession
import com.example.data.auth.UserSessionManager
import com.example.service.auth.GoogleAuthService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthAndCloudSyncTest {

    @Test
    fun testUserSessionSaveAndLoad() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val sessionManager = UserSessionManager(context)

        val session = AtriUserSession(
            userId = "usr_test_12345",
            email = "businessman@gmail.com",
            displayName = "Ramesh Shrestha",
            accountType = AccountType.BUSINESS,
            businessType = "Retail Shop",
            businessName = "Shrestha Kirana Store",
            isLoggedIn = true,
            isGuestMode = false,
            lastSyncTimestamp = 1700000000000L
        )

        sessionManager.saveSession(session)

        val loaded = sessionManager.currentSession.value
        assertNotNull(loaded)
        assertEquals("usr_test_12345", loaded?.userId)
        assertEquals("businessman@gmail.com", loaded?.email)
        assertEquals("Shrestha Kirana Store", loaded?.businessName)
        assertEquals(AccountType.BUSINESS, loaded?.accountType)
        assertTrue(loaded?.isLoggedIn == true)
        assertFalse(loaded?.isGuestMode == true)

        sessionManager.clearSession()
        assertEquals(null, sessionManager.currentSession.value)
    }

    @Test
    fun testDeterministicStableGoogleUserIdCreation() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val googleAuth = GoogleAuthService(context)

        val res1 = googleAuth.createAuthenticatedAccount("owner.nepal@gmail.com", "Nepal Owner")
        val res2 = googleAuth.createAuthenticatedAccount("owner.nepal@gmail.com", "Nepal Owner")

        assertTrue(res1.isSuccess)
        assertNotNull(res1.userId)
        assertEquals(res1.userId, res2.userId)
        assertEquals("owner.nepal@gmail.com", res1.email)
    }
}
