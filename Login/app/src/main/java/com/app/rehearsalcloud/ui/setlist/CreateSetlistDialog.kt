import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun CreateSetlistDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit,
    selectedDate: String,
    onDateChange: (String) -> Unit,
    onNameChange: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(selectedDate) }
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    // Get the Context to create DatePickerDialog
    val context = LocalContext.current

    // Handle DatePicker Dialog visibility
    val showDatePicker = remember { mutableStateOf(false) }

    // DatePickerDialog to select the date
    if (showDatePicker.value) {
        DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                val selectedLocalDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDayOfMonth)
                date = selectedLocalDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                onDateChange(date)
                showDatePicker.value = false
            },
            year,
            month,
            day
        ).show()
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Create a new setlist", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Setlist name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = { /* Do nothing, it's read-only */ },
                    label = { Text("Service date") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(onClick = { showDatePicker.value = true }) {
                            Text("Pick date")
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreate(name, date)
                    onDismiss()
                },
                enabled = name.trim().isNotEmpty()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}
