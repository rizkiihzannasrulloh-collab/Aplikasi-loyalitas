package com.example.ui.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Customer
import com.example.data.LoyaltyTransaction
import com.example.data.TransactionType
import com.example.ui.LoyaltyViewModel
import com.example.ui.components.LoyaltyCardView
import com.example.ui.components.RiceBowlIllustration
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NasiCardStroke
import com.example.ui.theme.NasiCream
import com.example.ui.theme.NasiCreamDark
import com.example.ui.theme.NasiOrange
import com.example.ui.theme.NasiOrangeContainer
import com.example.ui.theme.NasiOrangeDark
import com.example.ui.theme.NasiPurple
import com.example.ui.theme.NasiPurpleContainer
import com.example.ui.theme.NasiYellow
import com.example.ui.theme.SuccessGreen
import com.example.util.QrCodeHelper

@Composable
fun CustomerScreen(
    viewModel: LoyaltyViewModel,
    onSwitchToAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentCustomer by viewModel.currentCustomer.collectAsStateWithLifecycle()
    val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val transactions by viewModel.customerTransactions.collectAsStateWithLifecycle()

    var showRegisterDialog by remember { mutableStateOf(false) }
    var showSelectCustomerDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("customer_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Greeting & Role Switcher
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RiceBowlIllustration(
                        size = 38.dp,
                        showPlate = false,
                        showSparkles = true,
                        showFace = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "NASI COKOT",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = NasiPurple
                        )
                        Text(
                            text = "Kartu Member Pelanggan",
                            fontSize = 12.sp,
                            color = NasiOrangeDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Switch to Cashier / Admin Mode Button
                Surface(
                    onClick = onSwitchToAdmin,
                    shape = RoundedCornerShape(20.dp),
                    color = NasiPurpleContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NasiPurple),
                    modifier = Modifier.testTag("switch_to_cashier_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Ganti Role",
                            tint = NasiPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Mode Kasir",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NasiPurple
                        )
                    }
                }
            }
        }

        // 2. Active Customer Selector Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NasiCreamDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = NasiPurpleContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = NasiPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = currentCustomer?.name ?: "Pilih Akun",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NasiPurple
                            )
                            Text(
                                text = currentCustomer?.phone ?: "Belum terdaftar",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(
                            onClick = { showSelectCustomerDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Ganti", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showRegisterDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NasiPurple),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("register_customer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Daftar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Digital Loyalty Card (Original Card Replica)
        if (currentCustomer != null) {
            item {
                LoyaltyCardView(
                    customer = currentCustomer!!,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 4. Progress Summary & Reward Banner
            item {
                RewardProgressCard(customer = currentCustomer!!)
            }

            // 5. Expiry & Information Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NasiCreamDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = NasiPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Masa Berlaku Kartu",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = NasiPurple
                                )
                            }
                            Text(
                                text = QrCodeHelper.formatRemainingDays(currentCustomer!!.expiresAt),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (currentCustomer!!.isExpired) ErrorRed else SuccessGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Terdaftar pada: ${QrCodeHelper.formatDate(currentCustomer!!.registeredAt)} • Berlaku s/d ${QrCodeHelper.formatDate(currentCustomer!!.expiresAt)} (6 bulan). Jika melewati masa berlaku, stempel akan direset kembali ke 0 sesuai aturan toko.",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // 6. Transaction History Header & List
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = NasiPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Riwayat Transaksi Stempel",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NasiPurple
                        )
                    }

                    Text(
                        text = "${transactions.size} transaksi",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Belum Ada Riwayat Transaksi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NasiPurple
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tunjukkan kode QR kartu Anda ke kasir saat membeli Nasi Cokot untuk mengumpulkan stempel pertamamu!",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(transactions) { tx ->
                    TransactionItemRow(tx = tx)
                }
            }
        } else {
            // Empty state if no customers registered yet
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RiceBowlIllustration(size = 72.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum Ada Member",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NasiPurple
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Daftarkan diri Anda untuk mendapatkan Kartu Loyalitas Digital 'Beli 8 Gratis 1' Nasi Cokot sekarang!",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showRegisterDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NasiPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Daftar Sekarang")
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Dialog: Daftar Member Baru
    if (showRegisterDialog) {
        RegisterCustomerDialog(
            onDismiss = { showRegisterDialog = false },
            onRegister = { name, phone ->
                viewModel.registerNewCustomer(name, phone) {
                    showRegisterDialog = false
                }
            }
        )
    }

    // Dialog: Pilih / Cari Akun Pelanggan
    if (showSelectCustomerDialog) {
        SelectCustomerDialog(
            customers = allCustomers,
            currentSelectedId = currentCustomer?.id,
            onDismiss = { showSelectCustomerDialog = false },
            onSelect = { cust ->
                viewModel.selectCustomer(cust.id)
                showSelectCustomerDialog = false
            }
        )
    }
}

@Composable
private fun RewardProgressCard(customer: Customer) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (customer.isRewardReady) NasiOrangeContainer else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (customer.isRewardReady) NasiOrange else NasiCreamDark
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (customer.isRewardReady) "🎉 SIAP KLAIM GRATIS 1!" else "Progress Stempel",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = if (customer.isRewardReady) NasiOrangeDark else NasiPurple
                    )
                    Text(
                        text = if (customer.isRewardReady)
                            "8 dari 8 stempel terkumpul!"
                        else
                            "${customer.currentStamps} dari 8 stempel terkumpul",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (customer.isRewardReady) NasiOrange else NasiPurple,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "${customer.currentStamps}/8",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { (customer.currentStamps / 8f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (customer.isRewardReady) NasiOrange else NasiPurple,
                trackColor = NasiCreamDark,
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (customer.isRewardReady) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Celebration,
                        contentDescription = null,
                        tint = NasiOrangeDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Selamat! Tunjukkan kartu ini ke kasir untuk klaim 1 porsi Nasi Cokot gratis!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NasiOrangeDark
                    )
                }
            } else {
                Text(
                    text = "Tinggal ${customer.stampsRemaining} stempel lagi untuk mendapatkan 1 porsi Nasi Cokot GRATIS!",
                    fontSize = 12.sp,
                    color = NasiPurple,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TransactionItemRow(tx: LoyaltyTransaction) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NasiCreamDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (tx.type) {
                TransactionType.STAMP_ADDED -> Icons.Default.CheckCircle
                TransactionType.REWARD_CLAIMED -> Icons.Default.Redeem
                TransactionType.EXPIRED_RESET -> Icons.Default.Info
                TransactionType.CARD_RENEWED -> Icons.Default.CheckCircle
            }
            val iconColor = when (tx.type) {
                TransactionType.STAMP_ADDED -> NasiPurple
                TransactionType.REWARD_CLAIMED -> NasiOrangeDark
                TransactionType.EXPIRED_RESET -> ErrorRed
                TransactionType.CARD_RENEWED -> SuccessGreen
            }

            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.note.ifBlank {
                        when (tx.type) {
                            TransactionType.STAMP_ADDED -> "+1 Stempel"
                            TransactionType.REWARD_CLAIMED -> "Klaim Beli 8 Gratis 1"
                            TransactionType.EXPIRED_RESET -> "Reset Masa Berlaku"
                            TransactionType.CARD_RENEWED -> "Perpanjangan Kartu"
                        }
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = NasiPurple
                )
                Text(
                    text = QrCodeHelper.formatDateTime(tx.timestamp),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // Stamp Change indicator
            if (tx.type == TransactionType.STAMP_ADDED) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NasiPurpleContainer
                ) {
                    Text(
                        text = "${tx.stampsBefore} ➔ ${tx.stampsAfter}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NasiPurple,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else if (tx.type == TransactionType.REWARD_CLAIMED) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NasiOrangeContainer
                ) {
                    Text(
                        text = "GRATIS 1",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = NasiOrangeDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterCustomerDialog(
    onDismiss: () -> Unit,
    onRegister: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Daftar Member Nasi Cokot",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = NasiPurple
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Dapatkan kode QR eksklusif Anda untuk mengumpulkan 8 stempel dan raih 1 porsi GRATIS!",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorText = null },
                    label = { Text("Nama Lengkap") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_customer_name")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; errorText = null },
                    label = { Text("Nomor HP (WhatsApp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_customer_phone")
                )

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = ErrorRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isBlank()) {
                        errorText = "Nama tidak boleh kosong"
                    } else if (phone.trim().length < 8) {
                        errorText = "Nomor HP minimal 8 digit"
                    } else {
                        onRegister(name.trim(), phone.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NasiPurple),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_register_button")
            ) {
                Text("Daftar Sekarang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun SelectCustomerDialog(
    customers: List<Customer>,
    currentSelectedId: String?,
    onDismiss: () -> Unit,
    onSelect: (Customer) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(customers, search) {
        if (search.isBlank()) customers
        else customers.filter {
            it.name.contains(search, ignoreCase = true) || it.phone.contains(search)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pilih Akun Member",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = NasiPurple
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Cari nama atau no HP...", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered) { cust ->
                        val isSelected = cust.id == currentSelectedId
                        Surface(
                            onClick = { onSelect(cust) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) NasiPurpleContainer else Color(0xFFF7F7F7),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NasiPurple) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = cust.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = NasiPurple
                                    )
                                    Text(
                                        text = "${cust.phone} • ${cust.id}",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (cust.isRewardReady) NasiOrange else NasiYellow
                                ) {
                                    Text(
                                        text = "${cust.currentStamps}/8",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NasiPurple,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}
