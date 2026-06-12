package com.simplifybiz.ops.presentation.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.simplifybiz.ops.data.tasks.Priorities
import com.simplifybiz.ops.presentation.AppNavigator
import com.simplifybiz.ops.presentation.components.BackIcon
import com.simplifybiz.ops.presentation.components.OpsTopBar
import com.simplifybiz.ops.presentation.components.SegmentedControl
import com.simplifybiz.ops.presentation.components.WheelDatePickerSheet
import com.simplifybiz.ops.util.isIosPlatform
import com.simplifybiz.ops.presentation.Route
import com.simplifybiz.ops.presentation.theme.OpsBrand
import com.simplifybiz.ops.util.formatDateForDisplay
import com.simplifybiz.ops.util.millisToLocalDate
import com.simplifybiz.ops.util.toApiDateString
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitTaskScreen(navigator: AppNavigator) {
    val viewModel: SubmitTaskViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.submitted) {
        if (state.submitted) navigator.goto(Route.TasksHome)
    }

    Scaffold(
        topBar = {
            OpsTopBar(
                title = { Text("New Task") },
                navigationIcon = {
                    IconButton(onClick = { navigator.goto(Route.TasksHome) }) {
                        BackIcon()
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            LabeledField(
                title = "Project",
                value = state.project,
                onValueChange = viewModel::setProject,
                hint = "Name of the project",
                isError = state.validationErrors.containsKey("project"),
                errorText = state.validationErrors["project"]
            )
            Spacer(Modifier.height(14.dp))

            LabeledField(
                title = "Task",
                value = state.task,
                onValueChange = viewModel::setTask,
                hint = "Name of the task",
                singleLine = false,
                minLines = 2,
                isError = state.validationErrors.containsKey("task"),
                errorText = state.validationErrors["task"]
            )
            Spacer(Modifier.height(14.dp))

            LabeledField(
                title = "Description",
                value = state.description,
                onValueChange = viewModel::setDescription,
                hint = "Give the team the context they need",
                singleLine = false,
                minLines = 3
            )
            Spacer(Modifier.height(14.dp))

            LabeledField(
                title = "Expected outcomes",
                value = state.expectedOutcomes,
                onValueChange = viewModel::setExpectedOutcomes,
                hint = "Describe the expected outcome of the task",
                singleLine = false,
                minLines = 3
            )
            Spacer(Modifier.height(16.dp))

            Text("Priority", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            if (isIosPlatform) {
                SegmentedControl(
                    options = listOf(
                        "Low" to Priorities.LOW,
                        "Normal" to Priorities.NORMAL,
                        "High" to Priorities.HIGH
                    ),
                    selected = state.priority,
                    onSelect = viewModel::setPriority
                )
            } else {
                PriorityRow(state.priority, viewModel::setPriority)
            }
            Spacer(Modifier.height(14.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Due date", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = formatDateForDisplay(state.dateDue).ifBlank { state.dateDue },
                    onValueChange = { },
                    placeholder = { Text("Tap to pick a date") },
                    singleLine = true,
                    readOnly = true,
                    isError = state.validationErrors.containsKey("dateDue"),
                    supportingText = state.validationErrors["dateDue"]?.let { { Text(it) } },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Outlined.DateRange, contentDescription = "Pick date")
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors(),
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }
                )
            }
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = viewModel::submit,
                enabled = !state.submitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit Task")
                }
            }

            state.errorTitle?.let { title ->
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            title,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.titleSmall
                        )
                        state.errorBody?.let { body ->
                            Spacer(Modifier.height(4.dp))
                            Text(
                                body,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = viewModel::clearError) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker && isIosPlatform) {
        WheelDatePickerSheet(
            onDismiss = { showDatePicker = false },
            onConfirm = { date ->
                viewModel.setDateDue(date.toApiDateString())
                showDatePicker = false
            }
        )
    }

    if (showDatePicker && !isIosPlatform) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val date = millisToLocalDate(millis)
                            viewModel.setDateDue(date.toApiDateString())
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun LabeledField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    isError: Boolean = false,
    errorText: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(hint) },
            singleLine = singleLine,
            minLines = minLines,
            isError = isError,
            supportingText = errorText?.let { { Text(it) } },
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = OpsBrand.fieldFill,
    unfocusedContainerColor = OpsBrand.fieldFill,
    focusedBorderColor = OpsBrand.action,
    unfocusedBorderColor = OpsBrand.glassBorder,
    cursorColor = OpsBrand.action
)

@Composable
private fun PriorityRow(selected: String, onSelect: (String) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        PriorityChip("Low", Priorities.LOW, selected, onSelect)
        PriorityChip("Normal", Priorities.NORMAL, selected, onSelect)
        PriorityChip("High", Priorities.HIGH, selected, onSelect)
    }
}

@Composable
private fun PriorityChip(label: String, value: String, selected: String, onSelect: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected == value, onClick = { onSelect(value) })
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
