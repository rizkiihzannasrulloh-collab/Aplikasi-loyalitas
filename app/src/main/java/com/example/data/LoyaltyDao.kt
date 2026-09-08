package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LoyaltyDao {
    @Query("SELECT * FROM customers ORDER BY CASE WHEN lastTransactionAt IS NULL THEN 0 ELSE lastTransactionAt END DESC, registeredAt DESC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    fun getCustomerById(id: String): Flow<Customer?>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun findCustomerById(id: String): Customer?

    @Query("SELECT * FROM customers WHERE phone = :phone LIMIT 1")
    suspend fun findCustomerByPhone(phone: String): Customer?

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR id LIKE '%' || :query || '%' ORDER BY registeredAt DESC")
    fun searchCustomers(query: String): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<Customer>)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT 200")
    fun getAllTransactions(): Flow<List<LoyaltyTransaction>>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getTransactionsForCustomer(customerId: String): Flow<List<LoyaltyTransaction>>

    @Insert
    suspend fun insertTransaction(transaction: LoyaltyTransaction)

    @Insert
    suspend fun insertTransactions(transactions: List<LoyaltyTransaction>)

    @Query("SELECT COUNT(*) FROM customers")
    fun getCustomerCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM transactions WHERE type = 'REWARD_CLAIMED' AND timestamp >= :sinceTimestamp")
    fun getClaimsCountSince(sinceTimestamp: Long): Flow<Int>

    @Query("SELECT SUM(totalStampsEarned) FROM customers")
    fun getTotalStampsEarned(): Flow<Int?>
}
