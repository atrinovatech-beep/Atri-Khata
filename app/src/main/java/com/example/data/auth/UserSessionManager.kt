package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class UserSessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)

    private val _currentSession = MutableStateFlow(loadSession())
    val currentSession: StateFlow<AtriUserSession?> = _currentSession.asStateFlow()

    private fun loadSession(): AtriUserSession? {
        val jsonStr = prefs.getString(KEY_SESSION_JSON, null) ?: return null
        return try {
            val json = JSONObject(jsonStr)
            AtriUserSession(
                userId = json.optString("userId", ""),
                email = json.optString("email", ""),
                displayName = json.optString("displayName", "User"),
                photoUrl = if (json.has("photoUrl") && !json.isNull("photoUrl")) json.getString("photoUrl") else null,
                accountType = if (json.optString("accountType", "BUSINESS") == "PERSONAL") AccountType.PERSONAL else AccountType.BUSINESS,
                businessType = json.optString("businessType", "General Trade"),
                businessName = json.optString("businessName", "My Business"),
                isLoggedIn = json.optBoolean("isLoggedIn", false),
                isGuestMode = json.optBoolean("isGuestMode", false),
                lastSyncTimestamp = json.optLong("lastSyncTimestamp", 0L),
                syncState = SyncState.IDLE,
                syncMessage = "Ready"
            )
        } catch (e: Exception) {
            null
        }
    }

    fun saveSession(session: AtriUserSession) {
        val json = JSONObject().apply {
            put("userId", session.userId)
            put("email", session.email)
            put("displayName", session.displayName)
            put("photoUrl", session.photoUrl)
            put("accountType", session.accountType.name)
            put("businessType", session.businessType)
            put("businessName", session.businessName)
            put("isLoggedIn", session.isLoggedIn)
            put("isGuestMode", session.isGuestMode)
            put("lastSyncTimestamp", session.lastSyncTimestamp)
        }
        prefs.edit().putString(KEY_SESSION_JSON, json.toString()).apply()
        _currentSession.value = session
    }

    fun updateSyncState(state: SyncState, message: String, timestamp: Long = System.currentTimeMillis()) {
        val current = _currentSession.value ?: return
        val updated = current.copy(
            syncState = state,
            syncMessage = message,
            lastSyncTimestamp = timestamp
        )
        saveSession(updated)
    }

    fun clearSession() {
        prefs.edit().remove(KEY_SESSION_JSON).apply()
        _currentSession.value = null
    }

    companion object {
        private const val PREF_FILE_NAME = "atri_khata_user_session"
        private const val KEY_SESSION_JSON = "key_active_session_json"
    }
}
