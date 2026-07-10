package com.example.timetable.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.timetable.model.Subject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSubjectDialog(subject: Subject, onDismiss: () -> Unit, onSave: (Subject) -> Unit) {
    var name by remember { mutableStateOf(subject.name) }
    var teacher by remember { mutableStateOf(subject.teacher ?: "") }
    var room by remember { mutableStateOf(subject.room ?: "") }
    var color by remember { mutableIntStateOf(subject.color) }
    var credits by remember { mutableStateOf(if (subject.credits > 0) subject.credits.toString() else "") }
    var gradePoint by remember { mutableStateOf(if (subject.gradePoint > 0.0) subject.gradePoint.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Subject") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Subject Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = teacher, onValueChange = { teacher = it }, label = { Text("Teacher") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Room") }, modifier = Modifier.fillMaxWidth())
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = credits, 
                        onValueChange = { if (it.all { char -> char.isDigit() }) credits = it }, 
                        label = { Text("Credits") }, 
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = gradePoint, 
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) gradePoint = it }, 
                        label = { Text("Grade Point") }, 
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                    )
                }

                Text("Color")
                ColorPickerRow(selectedColor = color, onColorSelected = { color = it })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    subject.name = name
                    subject.teacher = teacher
                    subject.room = room
                    subject.color = color
                    subject.credits = credits.toIntOrNull() ?: 0
                    subject.gradePoint = gradePoint.toDoubleOrNull() ?: 0.0
                    onSave(subject)
                }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
