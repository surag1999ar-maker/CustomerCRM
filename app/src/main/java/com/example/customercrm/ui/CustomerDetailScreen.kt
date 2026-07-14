package com.example.customercrm.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.customercrm.data.CallLog
import com.example.customercrm.data.CallOutcome
import com.example.customercrm.data.Customer
import com.example.customercrm.data.FollowUp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customer: Customer,
    callLogs: List<CallLog>,
    followUps: List<FollowUp>,
    onBack: () -> Unit,
    onAddCallLog: (dateTime: Long, outcome: CallOutcome, notes: String) -> Unit,
    onAddFollowUp: (dueDateTime: Long, note: String) -> Unit,
    onMarkFollowUpDone: (FollowUp) -> Unit,
    onDeleteFollowUp: (FollowUp) -> Unit,
    onDeleteCustomer: () -> Unit
) {
    var showCallDialog by remember { mutableStateOf(false) }
    var showFollowUpDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customer.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete customer")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            item {
                if (customer.company.isNotBlank()) Text(customer.company, style = MaterialTheme.typography.bodyLarge)
                if (customer.phone.isNotBlank()) Text(customer.phone, style = MaterialTheme.typography.bodyMedium)
                if (customer.notes.isNotBlank()) Text(customer.notes, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))

                Row {
                    Button(onClick = { showCallDialog = true }, modifier = Modifier.weight(1f)) {
                        Text("Log a Call")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { showFollowUpDialog = true }, modifier = Modifier.weight(1f)) {
                        Text("Set Reminder")
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text("Follow-ups", style = MaterialTheme.typography.titleMedium)
            }

            if (followUps.isEmpty()) {
                item { Text("No follow-ups scheduled.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(followUps, key = { it.id }) { fu ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(formatDateTime(fu.dueDateTime))
                                if (fu.note.isNotBlank()) Text(fu.note, style = MaterialTheme.typography.bodyMedium)
                                if (fu.isDone) Text("Done", color = MaterialTheme.colorScheme.primary)
                            }
                            if (!fu.isDone) {
                                IconButton(onClick = { onMarkFollowUpDone(fu) }) {
                                    Icon(Icons.Default.Check, contentDescription = "Mark done")
                                }
                            }
                            IconButton(onClick = { onDeleteFollowUp(fu) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete follow-up")
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(20.dp))
                Text("Call History", style = MaterialTheme.typography.titleMedium)
            }

            if (callLogs.isEmpty()) {
                item { Text("No calls logged yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(callLogs, key = { it.id }) { log ->
                    ListItem(
                        headlineContent = { Text(log.outcome.name.replace('_', ' ')) },
                        supportingContent = {
                            Column {
                                Text(formatDateTime(log.callDateTime))
                                if (log.notes.isNotBlank()) Text(log.notes)
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showCallDialog) {
        LogCallDialog(
            onDismiss = { showCallDialog = false },
            onSave = { dateTime, outcome, notes ->
                onAddCallLog(dateTime, outcome, notes)
                showCallDialog = false
            }
        )
    }

    if (showFollowUpDialog) {
        SetFollowUpDialog(
            onDismiss = { showFollowUpDialog = false },
            onSave = { dateTime, note ->
                onAddFollowUp(dateTime, note)
                showFollowUpDialog = false
                Toast.makeText(context, "Reminder set \u2014 added to local calendar", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete ${customer.name}?") },
            text = { Text("This will remove the customer, their call history, and follow-ups.") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDeleteCustomer() }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun LogCallDialog(onDismiss: () -> Unit, onSave: (Long, CallOutcome, String) -> Unit) {
    val context = LocalContext.current
    var dateTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var outcome by remember { mutableStateOf(CallOutcome.CONNECTED) }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log a Call") },
        text = {
            Column {
                OutlinedButton(onClick = { pickDateTime(context) { dateTime = it } }) {
                    Text(formatDateTime(dateTime))
                }
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = outcome.name.replace('_', ' '),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Outcome") },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        CallOutcome.entries.forEach {
                            DropdownMenuItem(text = { Text(it.name.replace('_', ' ')) }, onClick = { outcome = it; expanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(dateTime, outcome, notes) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SetFollowUpDialog(onDismiss: () -> Unit, onSave: (Long, String) -> Unit) {
    val context = LocalContext.current
    var dateTime by remember { mutableStateOf<Long?>(null) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Follow-up Reminder") },
        text = {
            Column {
                OutlinedButton(onClick = { pickDateTime(context) { dateTime = it } }) {
                    Text(dateTime?.let { formatDateTime(it) } ?: "Choose date & time")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { dateTime?.let { onSave(it, note) } }, enabled = dateTime != null) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
