package com.example.data.auth

enum class AccountType {
    BUSINESS,
    PERSONAL
}

enum class SyncState {
    IDLE,
    SYNCING,
    SYNCED,
    OFFLINE,
    ERROR
}

data class AtriUserSession(
    val userId: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val accountType: AccountType = AccountType.BUSINESS,
    val businessType: String = "General Trade",
    val businessName: String = "My Business",
    val isLoggedIn: Boolean = false,
    val isGuestMode: Boolean = false,
    val lastSyncTimestamp: Long = 0L,
    val syncState: SyncState = SyncState.IDLE,
    val syncMessage: String = "All data saved locally"
)
