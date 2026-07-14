package com.example.customercrm.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.customercrm.data.Customer
import com.example.customercrm.data.FollowUp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    customers: List<Customer>,
    followUps: List<FollowUp>,
    onAddCustomer: (name: String, phone: String, company: String, notes: String) -> Unit,
    onOpenCustomer: (Long) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    val pendingFollowUps = followUps.filter { !it.isDone }
    val overdueCount = pendingFollowUps.count { it.dueDateTime < System.currentTimeMillis() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Customer CRM") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add customer")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            if (pendingFollowUps.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (overdueCount > 0) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (overdueCount > 0)
                                "$overdueCount overdue follow-up${if (overdueCount > 1) "s" else ""}, ${pendingFollowUps.size} total pending"
                            else
                                "${pendingFollowUps.size} upcoming follow-up${if (pendingFollowUps.size > 1) "s" else ""}"
                        )
                    }
                }
            }

            if (customers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No customers yet. Tap + to add one.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(customers, key = { it.id }) { customer ->
                        val nextFollowUp = pendingFollowUps
                            .filter { it.customerId == customer.id }
                            .minByOrNull { it.dueDateTime }

                        ListItem(
                            headlineContent = { Text(customer.name) },
                            supportingContent = {
                                Column {
                                    if (customer.company.isNotBlank()) Text(customer.company)
                                    if (customer.phone.isNotBlank()) Text(customer.phone)
                                    nextFollowUp?.let {
                                        Text(
                                            "Next follow-up: ${formatDateTime(it.dueDateTime)}",
                                            color = if (it.dueDateTime < System.currentTimeMillis())
                                                MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            leadingContent = { Icon(Icons.Default.Call, contentDescription = null) },
                            modifier = Modifier.clickable(customer.id, onOpenCustomer)
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCustomerDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, phone, company, notes ->
                onAddCustomer(name, phone, company, notes)
                showAddDialog = false
            }
        )
    }
}

private fun Modifier.clickable(id: Long, onClick: (Long) -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable { onClick(id) })

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerDialog(onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Customer") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name*") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Company") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onSave(name, phone, company, notes) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
