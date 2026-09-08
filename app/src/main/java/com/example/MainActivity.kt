package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppRole
import com.example.ui.LoyaltyViewModel
import com.example.ui.UiEvent
import com.example.ui.admin.AdminScreen
import com.example.ui.customer.CustomerScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NasiCokotLoyaltyApp()
            }
        }
    }
}

@Composable
fun NasiCokotLoyaltyApp(viewModel: LoyaltyViewModel = viewModel()) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is UiEvent.ScanSuccess -> {
                    // Handled inside verification modal
                }
                is UiEvent.StampAddedSuccess -> {
                    // Handled via toast message
                }
                is UiEvent.RewardClaimedSuccess -> {
                    // Handled via toast message
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentRole,
                label = "role_crossfade"
            ) { role ->
                when (role) {
                    AppRole.CUSTOMER -> {
                        CustomerScreen(
                            viewModel = viewModel,
                            onSwitchToAdmin = {
                                viewModel.setRole(AppRole.CASHIER_ADMIN)
                            }
                        )
                    }
                    AppRole.CASHIER_ADMIN -> {
                        AdminScreen(
                            viewModel = viewModel,
                            onSwitchToCustomer = {
                                viewModel.setRole(AppRole.CUSTOMER)
                            }
                        )
                    }
                }
            }
        }
    }
}
