package com.example.customercrm

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.customercrm.ui.CrmViewModel
import com.example.customercrm.ui.CustomerDetailScreen
import com.example.customercrm.ui.CustomerListScreen
import com.example.customercrm.ui.theme.CustomerCrmTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CrmViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* no-op: reminders still work with a fallback inexact alarm if denied */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNeededPermissions()

        setContent {
            CustomerCrmTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(viewModel)
                }
            }
        }
    }

    private fun requestNeededPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissions.add(Manifest.permission.READ_CALENDAR)
        permissions.add(Manifest.permission.WRITE_CALENDAR)
        requestPermissionLauncher.launch(permissions.toTypedArray())

        // Exact alarms need a separate settings-screen grant on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
            }
        }
    }
}

@Composable
fun AppNavHost(viewModel: CrmViewModel) {
    val navController = rememberNavController()
    val customers by viewModel.allCustomers.collectAsState()
    val followUps by viewModel.allFollowUps.collectAsState()

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            CustomerListScreen(
                customers = customers,
                followUps = followUps,
                onAddCustomer = { name, phone, company, notes -> viewModel.addCustomer(name, phone, company, notes) },
                onOpenCustomer = { id -> navController.navigate("detail/$id") }
            )
        }
        composable(
            "detail/{customerId}",
            arguments = listOf(navArgument("customerId") { type = NavType.LongType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: return@composable
            val customer by viewModel.getCustomer(customerId).collectAsState(initial = null)
            val callLogs by viewModel.getCallLogs(customerId).collectAsState(initial = emptyList())
            val customerFollowUps by viewModel.getFollowUps(customerId).collectAsState(initial = emptyList())

            customer?.let { c ->
                CustomerDetailScreen(
                    customer = c,
                    callLogs = callLogs,
                    followUps = customerFollowUps,
                    onBack = { navController.popBackStack() },
                    onAddCallLog = { dateTime, outcome, notes -> viewModel.addCallLog(customerId, dateTime, outcome, notes) },
                    onAddFollowUp = { dueDateTime, note -> viewModel.addFollowUp(customerId, c.name, dueDateTime, note) },
                    onMarkFollowUpDone = { viewModel.markFollowUpDone(it) },
                    onDeleteFollowUp = { viewModel.deleteFollowUp(it) },
                    onDeleteCustomer = {
                        viewModel.deleteCustomer(c)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
