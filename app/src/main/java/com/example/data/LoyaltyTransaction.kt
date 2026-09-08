package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    STAMP_ADDED,    // 1 stamp added
    REWARD_CLAIMED, // "Gratis 1" claimed, reset to 0
    EXPIRED_RESET,  // Stamped reset due to card expiration
    CARD_RENEWED    // Validity period extended
}

@Entity(tableName = "transactions")
data class LoyaltyTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: String,
    val customerName: String,
    val type: TransactionType,
    val stampsBefore: Int,
    val stampsAfter: Int,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
