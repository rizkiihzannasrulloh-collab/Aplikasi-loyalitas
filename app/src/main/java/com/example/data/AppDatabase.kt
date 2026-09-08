package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = runCatching {
        TransactionType.valueOf(value)
    }.getOrDefault(TransactionType.STAMP_ADDED)
}

@Database(
    entities = [Customer::class, LoyaltyTransaction::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun loyaltyDao(): LoyaltyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nasi_cokot_loyalty.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.loyaltyDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: LoyaltyDao) {
                val now = System.currentTimeMillis()
                val sixMonths = 180L * 24 * 60 * 60 * 1000L
                val oneDay = 24L * 60 * 60 * 1000L

                val demoCustomers = listOf(
                    Customer(
                        id = "NC-829104",
                        name = "Siti Rahma",
                        phone = "085712345678",
                        currentStamps = 8,
                        totalClaims = 1,
                        totalStampsEarned = 16,
                        registeredAt = now - (45 * oneDay),
                        expiresAt = now + sixMonths - (45 * oneDay),
                        lastTransactionAt = now - (2 * 3600 * 1000L)
                    ),
                    Customer(
                        id = "NC-419082",
                        name = "Budi Santoso",
                        phone = "081234567890",
                        currentStamps = 7,
                        totalClaims = 0,
                        totalStampsEarned = 7,
                        registeredAt = now - (30 * oneDay),
                        expiresAt = now + sixMonths - (30 * oneDay),
                        lastTransactionAt = now - (18 * 3600 * 1000L)
                    ),
                    Customer(
                        id = "NC-635194",
                        name = "Ahmad Fauzi",
                        phone = "082198765432",
                        currentStamps = 5,
                        totalClaims = 0,
                        totalStampsEarned = 5,
                        registeredAt = now - (15 * oneDay),
                        expiresAt = now + sixMonths - (15 * oneDay),
                        lastTransactionAt = now - (2 * oneDay)
                    ),
                    Customer(
                        id = "NC-749215",
                        name = "Dewi Lestari",
                        phone = "087811223344",
                        currentStamps = 3,
                        totalClaims = 0,
                        totalStampsEarned = 3,
                        registeredAt = now - (8 * oneDay),
                        expiresAt = now + sixMonths - (8 * oneDay),
                        lastTransactionAt = now - (3 * oneDay)
                    ),
                    Customer(
                        id = "NC-105829",
                        name = "Rizki Pratama",
                        phone = "082299231446",
                        currentStamps = 1,
                        totalClaims = 0,
                        totalStampsEarned = 1,
                        registeredAt = now - (2 * oneDay),
                        expiresAt = now + sixMonths - (2 * oneDay),
                        lastTransactionAt = now - (1 * oneDay)
                    )
                )

                dao.insertCustomers(demoCustomers)

                // Add some initial transactions
                val transactions = listOf(
                    LoyaltyTransaction(
                        customerId = "NC-829104",
                        customerName = "Siti Rahma",
                        type = TransactionType.STAMP_ADDED,
                        stampsBefore = 7,
                        stampsAfter = 8,
                        note = "Transaksi Nasi Cokot Ayam Crispy (Stempel ke-8)",
                        timestamp = now - (2 * 3600 * 1000L)
                    ),
                    LoyaltyTransaction(
                        customerId = "NC-419082",
                        customerName = "Budi Santoso",
                        type = TransactionType.STAMP_ADDED,
                        stampsBefore = 6,
                        stampsAfter = 7,
                        note = "Transaksi Paket Nasi Cokot Beef",
                        timestamp = now - (18 * 3600 * 1000L)
                    ),
                    LoyaltyTransaction(
                        customerId = "NC-829104",
                        customerName = "Siti Rahma",
                        type = TransactionType.REWARD_CLAIMED,
                        stampsBefore = 8,
                        stampsAfter = 0,
                        note = "Klaim Gratis 1 Nasi Cokot Rendang",
                        timestamp = now - (20 * oneDay)
                    )
                )

                dao.insertTransactions(transactions)
            }
        }
    }
}
