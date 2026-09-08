package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey
    val id: String, // Unique barcode/QR code, e.g. "NC-849201"
    val name: String,
    val phone: String,
    val currentStamps: Int = 0, // 0 to 8
    val totalClaims: Int = 0, // Number of free rewards claimed
    val totalStampsEarned: Int = 0, // Cumulative stamps collected
    val registeredAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (180L * 24 * 60 * 60 * 1000L), // 6 months default
    val lastTransactionAt: Long? = null
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() > expiresAt

    val isRewardReady: Boolean
        get() = currentStamps >= 8 && !isExpired

    val stampsRemaining: Int
        get() = (8 - currentStamps).coerceAtLeast(0)
}
