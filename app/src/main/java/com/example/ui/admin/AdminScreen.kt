package com.example.ui.admin

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Customer
import com.example.data.LoyaltyTransaction
import com.example.data.TransactionType
import com.example.ui.CustomerFilter
import com.example.ui.LoyaltyViewModel
import com.example.ui.UiEvent
import com.example.ui.components.LoyaltyCardView
import com.example.ui.components.RiceBowlIllustration
import com.example.ui.customer.RegisterCustomerDialog
import com.example.ui.scanner.CameraQrScanner
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NasiCardStroke
import com.example.ui.theme.NasiCream
import com.example.ui.theme.NasiCreamDark
import com.example.ui.theme.NasiOrange
import com.example.ui.theme.NasiOrangeContainer
import com.example.ui.theme.NasiOrangeDark
import com.example.ui.theme.NasiOrangeLight
import com.example.ui.theme.NasiPurple
import com.example.ui.theme.NasiPurpleContainer
import com.example.ui.theme.NasiYellow
import com.example.ui.theme.SuccessGreen
import com.example.util.QrCodeHelper

@Composable
fun AdminScreen(
    viewModel: LoyaltyViewModel,
    onSwitchToCustomer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Scan Kasir", "Dashboard", "Pelanggan", "Histori")

    val scannedCustomer by viewModel.scannedCustomer.collectAsStateWithLifecycle()
    val showScanDialog by viewModel.showScanDialog.collectAsStateWithLifecycle()

    var showRegisterDialog by remember { mutableStateOf(false) }
    var inspectedCustomer by remember { mutableStateOf<Customer?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_screen_container")
    ) {
        // 1. Top Header Bar
        Surface(
            color = NasiPurple,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RiceBowlIllustration(size = 32.dp, showPlate = false, showSparkles = false)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "KASIR & ADMIN",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Nasi Cokot Loyalty System",
                            color = NasiYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Switch back to Customer Card
                Surface(
                    onClick = onSwitchToCustomer,
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.18f),
                    modifier = Modifier.testTag("switch_to_customer_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Lihat Kartu",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Tab Navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = NasiPurple,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                val icon = when (index) {
                    0 -> Icons.Default.QrCodeScanner
                    1 -> Icons.Default.Dashboard
                    2 -> Icons.Default.People
                    else -> Icons.Default.History
                }
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("tab_$index")
                )
            }
        }

        HorizontalDivider(thickness = 1.dp, color = NasiCreamDark)

        // 3. Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> CashierScannerTab(
                    viewModel = viewModel,
                    onInspectCustomer = { inspectedCustomer = it }
                )
                1 -> DashboardStatsTab(viewModel = viewModel)
                2 -> CustomerManagementTab(
                    viewModel = viewModel,
                    onAddNewCustomer = { showRegisterDialog = true },
                    onInspectCustomer = { inspectedCustomer = it }
                )
                3 -> TransactionLogsTab(viewModel = viewModel)
            }
        }
    }

    // Modal Dialog: Verifikasi Scan Pelanggan Kasir (+1 Stempel / Klaim Hadiah)
    if (showScanDialog && scannedCustomer != null) {
        CashierScanVerificationDialog(
            customer = scannedCustomer!!,
            onDismiss = { viewModel.dismissScanDialog() },
            onAddStamp = { note ->
                viewModel.addStamp(scannedCustomer!!, note)
            },
            onClaimReward = { item ->
                viewModel.claimReward(scannedCustomer!!, item)
            },
            onRenewCard = {
                viewModel.renewCard(scannedCustomer!!)
            }
        )
    }

    // Modal Dialog: Preview Kartu Pelanggan
    if (inspectedCustomer != null) {
        AlertDialog(
            onDismissRequest = { inspectedCustomer = null },
            title = {
                Text(
                    text = "Detail Kartu Member",
                    fontWeight = FontWeight.Bold,
                    color = NasiPurple
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LoyaltyCardView(customer = inspectedCustomer!!)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.handleScannedCode(inspectedCustomer!!.id)
                        inspectedCustomer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NasiPurple)
                ) {
                    Text("Buka Aksi Kasir")
                }
            },
            dismissButton = {
                TextButton(onClick = { inspectedCustomer = null }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Dialog Tambah Member Baru
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
}

@Composable
private fun CashierScannerTab(
    viewModel: LoyaltyViewModel,
    onInspectCustomer: (Customer) -> Unit
) {
    val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()
    var manualInput by remember { mutableStateOf("") }
    var isManualOpen by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("cashier_scanner_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Scanner Viewfinder Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                CameraQrScanner(
                    onQrDecoded = { code ->
                        viewModel.handleScannedCode(code)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Quick Input / Fallback Card
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
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = null,
                                tint = NasiPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Input Manual / Cari Cepat",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = NasiPurple
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualInput,
                            onValueChange = { manualInput = it },
                            placeholder = { Text("Kode QR (NC-XXXXXX) / No HP...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_qr_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (manualInput.isNotBlank()) {
                                    viewModel.handleScannedCode(manualInput.trim())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NasiPurple),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("manual_search_button")
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Cari")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scan")
                        }
                    }
                }
            }
        }

        // Quick Test / Simulasi Scan Dropdown Bar
        item {
            Column {
                Text(
                    text = "⚡ Simulasi / Pilih Pelanggan untuk Scan Cepat:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NasiPurple
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allCustomers) { cust ->
                        Surface(
                            onClick = { viewModel.handleScannedCode(cust.id) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (cust.isRewardReady) NasiOrangeContainer else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (cust.isRewardReady) NasiOrangeDark else NasiPurple.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = cust.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = NasiPurple
                                    )
                                    Text(
                                        text = cust.id,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (cust.isRewardReady) NasiOrangeDark else NasiPurple
                                ) {
                                    Text(
                                        text = if (cust.isRewardReady) "KLAIM!" else "${cust.currentStamps}/8",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CashierScanVerificationDialog(
    customer: Customer,
    onDismiss: () -> Unit,
    onAddStamp: (String) -> Unit,
    onClaimReward: (String) -> Unit,
    onRenewCard: () -> Unit
) {
    var note by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf("1 Porsi Nasi Cokot Rendang") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Verifikasi Pelanggan",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = NasiPurple
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Tutup")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Customer Profile Verification Box
                Surface(
                    color = NasiPurpleContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = NasiPurple,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = customer.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = customer.name,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = NasiPurple
                            )
                            Text(
                                text = "📱 ${customer.phone}  •  ID: ${customer.id}",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "Masa berlaku: ${QrCodeHelper.formatDate(customer.expiresAt)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (customer.isExpired) ErrorRed else SuccessGreen
                            )
                        }
                    }
                }

                // Stamp Status Preview
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (customer.isRewardReady) NasiOrangeContainer else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (customer.isRewardReady) NasiOrangeDark else NasiCreamDark
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (customer.isRewardReady)
                                    "🎉 TARGET 8 STEMPEL TERCAPAI!"
                                else
                                    "Status Stempel Saat Ini",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (customer.isRewardReady) NasiOrangeDark else NasiPurple
                            )
                            Text(
                                text = if (customer.isRewardReady)
                                    "Pelanggan berhak klaim 1 porsi GRATIS!"
                                else
                                    "Kurang ${customer.stampsRemaining} stempel lagi untuk Beli 8 Gratis 1",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (customer.isRewardReady) NasiOrangeDark else NasiPurple
                        ) {
                            Text(
                                text = "${customer.currentStamps}/8",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (customer.isExpired) {
                    Text(
                        text = "⚠️ Kartu pelanggan ini telah melewati masa berlaku. Menambah stempel akan mereset progress & memulai siklus kartu baru.",
                        color = ErrorRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Action Buttons for Cashier
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (customer.isRewardReady) {
                        // Big Prominent "KLAIM GRATIS 1" Button
                        Button(
                            onClick = {
                                onClaimReward(selectedItem)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NasiOrangeDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("claim_reward_button")
                        ) {
                            Icon(imageVector = Icons.Default.Celebration, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🎁 KLAIM GRATIS 1 (RESET KE 0)",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        // Standard "+1 STEMPEL" Button
                        Button(
                            onClick = {
                                onAddStamp(note.ifBlank { "Transaksi Kasir" })
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NasiPurple),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("add_stamp_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "+1 STEMPEL KASIR",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun DashboardStatsTab(viewModel: LoyaltyViewModel) {
    val customerCount by viewModel.customerCount.collectAsStateWithLifecycle()
    val claimsThisMonth by viewModel.claimsThisMonth.collectAsStateWithLifecycle()
    val totalStampsEarned by viewModel.totalStampsEarned.collectAsStateWithLifecycle()
    val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()

    val topCustomers = remember(allCustomers) {
        allCustomers.sortedByDescending { it.totalStampsEarned + (it.totalClaims * 8) }.take(5)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_stats_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Metric Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Total Pelanggan",
                    value = "$customerCount",
                    subtitle = "Member terdaftar",
                    icon = Icons.Default.People,
                    color = NasiPurple,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Klaim Bulan Ini",
                    value = "$claimsThisMonth",
                    subtitle = "Hadiah Gratis 1",
                    icon = Icons.Default.Redeem,
                    color = NasiOrangeDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Total Stempel",
                    value = "${totalStampsEarned ?: 0}",
                    subtitle = "Diberikan ke member",
                    icon = Icons.Default.CheckCircle,
                    color = NasiOrange,
                    modifier = Modifier.weight(1f)
                )

                val readyClaimCount = allCustomers.count { it.isRewardReady }
                MetricCard(
                    title = "Siap Klaim",
                    value = "$readyClaimCount",
                    subtitle = "Stempel 8/8",
                    icon = Icons.Default.Celebration,
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Leaderboard: Pelanggan Paling Aktif
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NasiCreamDark)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Leaderboard,
                                contentDescription = null,
                                tint = NasiPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pelanggan Paling Aktif",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = NasiPurple
                            )
                        }

                        Text(
                            text = "Top 5",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NasiOrangeDark
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    topCustomers.forEachIndexed { index, cust ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = when (index) {
                                        0 -> NasiOrange
                                        1 -> NasiYellow
                                        2 -> NasiOrangeLight
                                        else -> NasiPurpleContainer
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "#${index + 1}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = NasiPurple
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = cust.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = NasiPurple
                                    )
                                    Text(
                                        text = "${cust.phone} • ${cust.totalClaims}x klaim gratis",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NasiPurpleContainer
                            ) {
                                Text(
                                    text = "${cust.totalStampsEarned} stempel",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NasiPurple,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        if (index < topCustomers.size - 1) {
                            HorizontalDivider(thickness = 0.5.dp, color = NasiCreamDark)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, NasiCreamDark),
        modifier = modifier
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
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )

                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.12f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = NasiPurple
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun CustomerManagementTab(
    viewModel: LoyaltyViewModel,
    onAddNewCustomer: () -> Unit,
    onInspectCustomer: (Customer) -> Unit
) {
    val filteredCustomers by viewModel.filteredCustomers.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("customer_management_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search & Add Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Cari nama, no HP, atau ID...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = NasiPurple)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("customer_search_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onAddNewCustomer,
                    colors = ButtonDefaults.buttonColors(containerColor = NasiPurple),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_customer_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            }
        }

        // Filter Chips
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = activeFilter == CustomerFilter.ALL,
                    onClick = { viewModel.activeFilter.value = CustomerFilter.ALL },
                    label = { Text("Semua", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = activeFilter == CustomerFilter.READY_CLAIM,
                    onClick = { viewModel.activeFilter.value = CustomerFilter.READY_CLAIM },
                    label = { Text("Siap Klaim (8)", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NasiOrangeContainer,
                        selectedLabelColor = NasiOrangeDark
                    )
                )
                FilterChip(
                    selected = activeFilter == CustomerFilter.ACTIVE,
                    onClick = { viewModel.activeFilter.value = CustomerFilter.ACTIVE },
                    label = { Text("Aktif", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = activeFilter == CustomerFilter.EXPIRED,
                    onClick = { viewModel.activeFilter.value = CustomerFilter.EXPIRED },
                    label = { Text("Kadaluwarsa", fontSize = 11.sp) }
                )
            }
        }

        if (filteredCustomers.isEmpty()) {
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
                            text = "Tidak Ada Pelanggan Ditemukan",
                            fontWeight = FontWeight.Bold,
                            color = NasiPurple
                        )
                    }
                }
            }
        } else {
            items(filteredCustomers) { cust ->
                CustomerAdminRow(
                    customer = cust,
                    onScanAction = { viewModel.handleScannedCode(cust.id) },
                    onInspect = { onInspectCustomer(cust) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun CustomerAdminRow(
    customer: Customer,
    onScanAction: () -> Unit,
    onInspect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (customer.isRewardReady) NasiOrangeContainer else Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (customer.isRewardReady) NasiOrangeDark else NasiCreamDark
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (customer.isRewardReady) NasiOrangeDark else NasiPurple,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = customer.name.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = customer.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NasiPurple
                    )
                    Text(
                        text = "${customer.phone}  •  ${customer.id}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Kadaluwarsa: ${QrCodeHelper.formatDate(customer.expiresAt)}",
                        fontSize = 10.sp,
                        color = if (customer.isExpired) ErrorRed else Color.DarkGray
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // View Card Button
                IconButton(
                    onClick = onInspect,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Lihat Kartu",
                        tint = NasiPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Quick Scan / Stamping Button
                Surface(
                    onClick = onScanAction,
                    shape = RoundedCornerShape(8.dp),
                    color = if (customer.isRewardReady) NasiOrangeDark else NasiPurple
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (customer.isRewardReady) "KLAIM!" else "${customer.currentStamps}/8",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionLogsTab(viewModel: LoyaltyViewModel) {
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("transaction_logs_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Audit Trail Riwayat Transaksi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = NasiPurple
                )
                Text(
                    text = "${transactions.size} riwayat",
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
                            text = "Belum Ada Catatan Transaksi",
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(transactions) { tx ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NasiCreamDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = when (tx.type) {
                                TransactionType.STAMP_ADDED -> NasiPurple.copy(alpha = 0.12f)
                                TransactionType.REWARD_CLAIMED -> NasiOrangeDark.copy(alpha = 0.15f)
                                TransactionType.EXPIRED_RESET -> ErrorRed.copy(alpha = 0.12f)
                                TransactionType.CARD_RENEWED -> SuccessGreen.copy(alpha = 0.12f)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (tx.type) {
                                        TransactionType.STAMP_ADDED -> Icons.Default.Add
                                        TransactionType.REWARD_CLAIMED -> Icons.Default.Celebration
                                        TransactionType.EXPIRED_RESET -> Icons.Default.Refresh
                                        TransactionType.CARD_RENEWED -> Icons.Default.Check
                                    },
                                    contentDescription = null,
                                    tint = when (tx.type) {
                                        TransactionType.STAMP_ADDED -> NasiPurple
                                        TransactionType.REWARD_CLAIMED -> NasiOrangeDark
                                        TransactionType.EXPIRED_RESET -> ErrorRed
                                        TransactionType.CARD_RENEWED -> SuccessGreen
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = tx.customerName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = NasiPurple
                                )
                                Text(
                                    text = QrCodeHelper.formatDateTime(tx.timestamp),
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }

                            Text(
                                text = tx.note.ifBlank { tx.type.name },
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        if (tx.type == TransactionType.STAMP_ADDED) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NasiPurpleContainer
                            ) {
                                Text(
                                    text = "${tx.stampsBefore}➔${tx.stampsAfter}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NasiPurple,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else if (tx.type == TransactionType.REWARD_CLAIMED) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NasiOrangeContainer
                            ) {
                                Text(
                                    text = "KLAIM",
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
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
