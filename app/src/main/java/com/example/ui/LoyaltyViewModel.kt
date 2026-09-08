package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Customer
import com.example.data.LoyaltyRepository
import com.example.data.LoyaltyTransaction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppRole {
    CUSTOMER,
    CASHIER_ADMIN
}

enum class CustomerFilter {
    ALL,
    READY_CLAIM, // currentStamps == 8
    ACTIVE,
    EXPIRED
}

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class ScanSuccess(val customer: Customer) : UiEvent()
    data class StampAddedSuccess(val customer: Customer, val newCount: Int) : UiEvent()
    data class RewardClaimedSuccess(val customer: Customer) : UiEvent()
}

class LoyaltyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LoyaltyRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = LoyaltyRepository(db.loyaltyDao())
    }

    // Role state
    private val _currentRole = MutableStateFlow(AppRole.CUSTOMER)
    val currentRole: StateFlow<AppRole> = _currentRole.asStateFlow()

    // Customer View: Active selected customer
    private val _selectedCustomerId = MutableStateFlow<String?>(null)
    val selectedCustomerId: StateFlow<String?> = _selectedCustomerId.asStateFlow()

    // Search and Filter for Admin
    val searchQuery = MutableStateFlow("")
    val activeFilter = MutableStateFlow(CustomerFilter.ALL)

    // Cashier Scanned Customer Modal State
    private val _scannedCustomer = MutableStateFlow<Customer?>(null)
    val scannedCustomer: StateFlow<Customer?> = _scannedCustomer.asStateFlow()

    private val _showScanDialog = MutableStateFlow(false)
    val showScanDialog: StateFlow<Boolean> = _showScanDialog.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

    // Statistics
    val customerCount: StateFlow<Int> = repository.customerCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val claimsThisMonth: StateFlow<Int> = repository.getClaimsCountThisMonth()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalStampsEarned: StateFlow<Int?> = repository.getTotalStampsEarned()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // All raw customers
    val allCustomers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All transactions
    val allTransactions: StateFlow<List<LoyaltyTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Customers
    val filteredCustomers: StateFlow<List<Customer>> = combine(
        allCustomers,
        searchQuery,
        activeFilter
    ) { customers, query, filter ->
        val q = query.trim().lowercase()
        customers.filter { c ->
            val matchesQuery = q.isEmpty() ||
                    c.name.lowercase().contains(q) ||
                    c.phone.contains(q) ||
                    c.id.lowercase().contains(q)

            val matchesFilter = when (filter) {
                CustomerFilter.ALL -> true
                CustomerFilter.READY_CLAIM -> c.isRewardReady
                CustomerFilter.ACTIVE -> !c.isExpired && c.currentStamps < 8
                CustomerFilter.EXPIRED -> c.isExpired
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Customer for Customer Screen
    val currentCustomer: StateFlow<Customer?> = combine(
        allCustomers,
        selectedCustomerId
    ) { customers, selectedId ->
        if (selectedId != null) {
            customers.firstOrNull { it.id == selectedId }
        } else {
            // Default to the first customer with highest activity/stamps for great demo preview
            customers.firstOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Customer's personal transactions
    val customerTransactions: StateFlow<List<LoyaltyTransaction>> = combine(
        allTransactions,
        currentCustomer
    ) { txs, cust ->
        if (cust == null) emptyList()
        else txs.filter { it.customerId == cust.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setRole(role: AppRole) {
        _currentRole.value = role
    }

    fun selectCustomer(customerId: String) {
        _selectedCustomerId.value = customerId
    }

    // Cashier scans code or enters ID/phone
    fun handleScannedCode(rawCode: String) {
        viewModelScope.launch {
            val customer = repository.findCustomerByIdOrPhone(rawCode)
            if (customer != null) {
                vibrateDevice()
                _scannedCustomer.value = customer
                _showScanDialog.value = true
                _eventFlow.emit(UiEvent.ScanSuccess(customer))
            } else {
                _eventFlow.emit(UiEvent.ShowToast("QR / ID '$rawCode' tidak ditemukan dalam sistem!"))
            }
        }
    }

    fun dismissScanDialog() {
        _showScanDialog.value = false
        _scannedCustomer.value = null
    }

    fun addStamp(customer: Customer, note: String = "Transaksi Nasi Cokot") {
        viewModelScope.launch {
            val result = repository.addStamp(customer.id, note)
            result.onSuccess { updated ->
                vibrateDevice()
                _scannedCustomer.value = updated
                _eventFlow.emit(UiEvent.StampAddedSuccess(updated, updated.currentStamps))
                if (updated.currentStamps == 8) {
                    _eventFlow.emit(UiEvent.ShowToast("🎉 SELAMAT! Stempel ke-8 selesai! Gratis 1 siap diklaim!"))
                } else {
                    _eventFlow.emit(UiEvent.ShowToast("Berhasil tambah 1 stempel untuk ${updated.name} (${updated.currentStamps}/8)"))
                }
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowToast(err.message ?: "Gagal menambah stempel"))
            }
        }
    }

    fun claimReward(customer: Customer, itemName: String = "1 Porsi Nasi Cokot") {
        viewModelScope.launch {
            val result = repository.claimReward(customer.id, itemName)
            result.onSuccess { updated ->
                vibrateDevice()
                _scannedCustomer.value = updated
                _eventFlow.emit(UiEvent.RewardClaimedSuccess(updated))
                _eventFlow.emit(UiEvent.ShowToast("🎁 Klaim Gratis 1 berhasil untuk ${updated.name}! Stempel direset ke 0."))
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowToast(err.message ?: "Gagal klaim hadiah"))
            }
        }
    }

    fun registerNewCustomer(name: String, phone: String, onComplete: ((Customer) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.registerCustomer(name, phone)
            result.onSuccess { newCust ->
                _selectedCustomerId.value = newCust.id
                _eventFlow.emit(UiEvent.ShowToast("Pelanggan ${newCust.name} berhasil terdaftar!"))
                onComplete?.invoke(newCust)
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowToast(err.message ?: "Gagal mendaftarkan pelanggan"))
            }
        }
    }

    fun renewCard(customer: Customer) {
        viewModelScope.launch {
            val result = repository.renewCard(customer.id, 6)
            result.onSuccess { updated ->
                _scannedCustomer.value = updated
                _eventFlow.emit(UiEvent.ShowToast("Masa berlaku kartu ${updated.name} diperpanjang 6 bulan!"))
            }
        }
    }

    private fun vibrateDevice() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(100)
                }
            }
        } catch (e: Exception) {
            // Ignore if vibration not permitted or not supported
        }
    }
}
