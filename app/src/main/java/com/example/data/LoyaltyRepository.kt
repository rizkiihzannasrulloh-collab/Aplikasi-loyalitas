package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Random

class LoyaltyRepository(private val dao: LoyaltyDao) {

    val allCustomers: Flow<List<Customer>> = dao.getAllCustomers()
    val allTransactions: Flow<List<LoyaltyTransaction>> = dao.getAllTransactions()
    val customerCount: Flow<Int> = dao.getCustomerCount()

    fun getTransactionsForCustomer(customerId: String): Flow<List<LoyaltyTransaction>> {
        return dao.getTransactionsForCustomer(customerId)
    }

    fun getCustomer(id: String): Flow<Customer?> {
        return dao.getCustomerById(id)
    }

    fun searchCustomers(query: String): Flow<List<Customer>> {
        return dao.searchCustomers(query)
    }

    fun getClaimsCountThisMonth(): Flow<Int> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return dao.getClaimsCountSince(cal.timeInMillis)
    }

    fun getTotalStampsEarned(): Flow<Int?> {
        return dao.getTotalStampsEarned()
    }

    suspend fun registerCustomer(name: String, phone: String): Result<Customer> {
        val cleanName = name.trim()
        val cleanPhone = phone.trim().replace(Regex("[^0-9+]"), "")

        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("Nama tidak boleh kosong"))
        }
        if (cleanPhone.length < 8) {
            return Result.failure(IllegalArgumentException("Nomor HP minimal 8 digit"))
        }

        // Check if phone already registered
        val existing = dao.findCustomerByPhone(cleanPhone)
        if (existing != null) {
            return Result.success(existing)
        }

        // Generate unique code like NC-XXXXXX
        val randomNum = 100000 + Random().nextInt(900000)
        val customerId = "NC-$randomNum"

        val now = System.currentTimeMillis()
        val sixMonths = 180L * 24 * 60 * 60 * 1000L

        val newCustomer = Customer(
            id = customerId,
            name = cleanName,
            phone = cleanPhone,
            currentStamps = 0,
            totalClaims = 0,
            totalStampsEarned = 0,
            registeredAt = now,
            expiresAt = now + sixMonths,
            lastTransactionAt = now
        )

        dao.insertCustomer(newCustomer)

        dao.insertTransaction(
            LoyaltyTransaction(
                customerId = newCustomer.id,
                customerName = newCustomer.name,
                type = TransactionType.STAMP_ADDED,
                stampsBefore = 0,
                stampsAfter = 0,
                note = "Pendaftaran member baru Nasi Cokot",
                timestamp = now
            )
        )

        return Result.success(newCustomer)
    }

    suspend fun findCustomerByIdOrPhone(identifier: String): Customer? {
        val clean = identifier.trim()
        // Try finding by direct ID
        val byId = dao.findCustomerById(clean)
        if (byId != null) return byId

        // Try format without prefix or with prefix
        val altId = if (clean.startsWith("NC-", ignoreCase = true)) clean else "NC-$clean"
        val byAltId = dao.findCustomerById(altId)
        if (byAltId != null) return byAltId

        // Try by phone
        return dao.findCustomerByPhone(clean)
    }

    suspend fun addStamp(customerId: String, note: String = "Transaksi Nasi Cokot"): Result<Customer> {
        val customer = dao.findCustomerById(customerId)
            ?: return Result.failure(IllegalArgumentException("Pelanggan dengan ID $customerId tidak ditemukan"))

        val now = System.currentTimeMillis()

        // Check expiration
        if (customer.isExpired) {
            // Business rule: kartu lewat masa berlaku -> reset
            val resetCustomer = customer.copy(
                currentStamps = 1,
                totalStampsEarned = customer.totalStampsEarned + 1,
                lastTransactionAt = now,
                expiresAt = now + (180L * 24 * 60 * 60 * 1000L) // renew
            )
            dao.updateCustomer(resetCustomer)
            dao.insertTransaction(
                LoyaltyTransaction(
                    customerId = customer.id,
                    customerName = customer.name,
                    type = TransactionType.EXPIRED_RESET,
                    stampsBefore = customer.currentStamps,
                    stampsAfter = 1,
                    note = "Masa berlaku kartu habis, stempel direset & dimulai siklus baru",
                    timestamp = now
                )
            )
            return Result.success(resetCustomer)
        }

        if (customer.currentStamps >= 8) {
            return Result.failure(IllegalStateException("Kartu sudah mencapai 8 stempel! Silakan klaim Gratis 1 terlebih dahulu."))
        }

        val newStamps = customer.currentStamps + 1
        val updatedCustomer = customer.copy(
            currentStamps = newStamps,
            totalStampsEarned = customer.totalStampsEarned + 1,
            lastTransactionAt = now
        )

        dao.updateCustomer(updatedCustomer)

        val txNote = if (newStamps == 8) {
            "Stempel ke-8 berhasil! Status: GRATIS 1 SIAP DIKLAIM 🎉"
        } else {
            note.ifBlank { "Tambah 1 Stempel ($newStamps/8)" }
        }

        dao.insertTransaction(
            LoyaltyTransaction(
                customerId = customer.id,
                customerName = customer.name,
                type = TransactionType.STAMP_ADDED,
                stampsBefore = customer.currentStamps,
                stampsAfter = newStamps,
                note = txNote,
                timestamp = now
            )
        )

        return Result.success(updatedCustomer)
    }

    suspend fun claimReward(customerId: String, itemReward: String = "Nasi Cokot Spesial"): Result<Customer> {
        val customer = dao.findCustomerById(customerId)
            ?: return Result.failure(IllegalArgumentException("Pelanggan tidak ditemukan"))

        if (customer.currentStamps < 8) {
            return Result.failure(IllegalStateException("Belum mencapai 8 stempel (saat ini ${customer.currentStamps}/8)"))
        }

        val now = System.currentTimeMillis()
        val updated = customer.copy(
            currentStamps = 0, // Reset ke 0 sesuai aturan bisnis
            totalClaims = customer.totalClaims + 1,
            lastTransactionAt = now
        )

        dao.updateCustomer(updated)

        dao.insertTransaction(
            LoyaltyTransaction(
                customerId = customer.id,
                customerName = customer.name,
                type = TransactionType.REWARD_CLAIMED,
                stampsBefore = 8,
                stampsAfter = 0,
                note = "Klaim Beli 8 Gratis 1 ($itemReward) berhasil!",
                timestamp = now
            )
        )

        return Result.success(updated)
    }

    suspend fun renewCard(customerId: String, months: Int = 6): Result<Customer> {
        val customer = dao.findCustomerById(customerId)
            ?: return Result.failure(IllegalArgumentException("Pelanggan tidak ditemukan"))

        val now = System.currentTimeMillis()
        val extraTime = months.toLong() * 30L * 24 * 60 * 60 * 1000L
        val baseTime = if (customer.expiresAt > now) customer.expiresAt else now
        val updated = customer.copy(expiresAt = baseTime + extraTime)

        dao.updateCustomer(updated)

        dao.insertTransaction(
            LoyaltyTransaction(
                customerId = customer.id,
                customerName = customer.name,
                type = TransactionType.CARD_RENEWED,
                stampsBefore = customer.currentStamps,
                stampsAfter = customer.currentStamps,
                note = "Perpanjangan masa berlaku kartu selama $months bulan",
                timestamp = now
            )
        )

        return Result.success(updated)
    }

    suspend fun deleteCustomer(customer: Customer) {
        dao.deleteCustomer(customer)
    }
}
