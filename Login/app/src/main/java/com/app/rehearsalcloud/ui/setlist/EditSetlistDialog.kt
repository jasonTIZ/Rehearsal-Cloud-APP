package com.app.rehearsalcloud.ui.setlist

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat

@Composable
fun EditSetlistDialog(
    setlistId: Int?,
    onDismiss: () -> Unit,
    onEdit: (Int, String, String) -> Unit,
    initialName: String?,
    initialDate: String?,
    onDateChange: (String) -> Unit,
    onNameChange: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName ?: "") }
    var date by remember { mutableStateOf(initialDate ?: "MM/dd/yyyy") }
    var dateError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                if (setlistId == null) "Create a new setlist" else "Edit Setlist",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        onNameChange(it)
                    },
                    label = { Text("Setlist name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = name.trim().isEmpty()
                )
                if (name.trim().isEmpty()) {
                    Text(
                        text = "Name cannot be empty",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = { /* Read-only */ },
                    label = { Text("Service date (MM/dd/yyyy)") },
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDatePicker(context) { selectedDate ->
                                date = selectedDate
                                onDateChange(selectedDate)
                                dateError = if (!validateDate(selectedDate)) {
                                    "Invalid date format. Use MM/dd/yyyy"
                                } else {
                                    null
                                }
                            }
                        },
                    isError = dateError != null || date == "MM/dd/yyyy"
                )
                if (dateError != null) {
                    Text(
                        text = dateError!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                if (date == "MM/dd/yyyy") {
                    Text(
                        text = "Please select a date",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (setlistId != null) {
                        onEdit(setlistId, name, date)
                    } else {
                        onEdit(0, name, date)
                    }
                    onDismiss()
                },
                enabled = name.trim().isNotEmpty() && date != "MM/dd/yyyy" && dateError == null
            ) {
                Text(if (setlistId == null) "Create" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

fun validateDate(dateString: String): Boolean {
    return try {
        val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        inputFormat.isLenient = false
        inputFormat.parse(dateString) != null
    } catch (e: Exception) {
        false
    }
}

fun showDatePicker(context: Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format(
                Locale.US,
                "%02d/%02d/%04d",
                selectedMonth + 1,
                selectedDay,
                selectedYear
            )
            onDateSelected(formattedDate)
        },
        year,
        month,
        day
    ).show()
}