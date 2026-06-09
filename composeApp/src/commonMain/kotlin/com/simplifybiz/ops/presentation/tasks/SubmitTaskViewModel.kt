package com.simplifybiz.ops.presentation.tasks

import com.simplifybiz.ops.data.ApiErrorMessages
import com.simplifybiz.ops.data.tasks.Priorities
import com.simplifybiz.ops.data.tasks.SubmitTaskPayload
import com.simplifybiz.ops.data.tasks.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SubmitTaskState(
    val project: String = "",
    val task: String = "",
    val addDescription: Boolean = false,
    val description: String = "",
    val addExpectedOutcomes: Boolean = false,
    val expectedOutcomes: String = "",
    val priority: String = Priorities.NORMAL,
    val dateDue: String = "",
    val submitting: Boolean = false,
    val errorTitle: String? = null,
    val errorBody: String? = null,
    val submitted: Boolean = false,
    val validationErrors: Map<String, String> = emptyMap()
)

class SubmitTaskViewModel(
    private val taskRepository: TaskRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(SubmitTaskState())
    val state: StateFlow<SubmitTaskState> = _state.asStateFlow()

    fun setProject(value: String) { _state.value = _state.value.copy(project = value, validationErrors = _state.value.validationErrors - "project") }
    fun setTask(value: String) { _state.value = _state.value.copy(task = value, validationErrors = _state.value.validationErrors - "task") }
    fun setAddDescription(value: Boolean) { _state.value = _state.value.copy(addDescription = value) }
    fun setDescription(value: String) { _state.value = _state.value.copy(description = value) }
    fun setAddExpectedOutcomes(value: Boolean) { _state.value = _state.value.copy(addExpectedOutcomes = value) }
    fun setExpectedOutcomes(value: String) { _state.value = _state.value.copy(expectedOutcomes = value) }
    fun setPriority(value: String) { _state.value = _state.value.copy(priority = value) }
    fun setDateDue(value: String) { _state.value = _state.value.copy(dateDue = value, validationErrors = _state.value.validationErrors - "dateDue") }

    fun clearError() { _state.value = _state.value.copy(errorTitle = null, errorBody = null) }

    fun submit() {
        val s = _state.value
        val errors = mutableMapOf<String, String>()

        if (s.task.isBlank()) errors["task"] = "Task required"
        if (s.project.isBlank()) errors["project"] = "Project required"
        if (s.dateDue.isBlank()) errors["dateDue"] = "Due date required"

        if (errors.isNotEmpty()) {
            _state.value = s.copy(validationErrors = errors)
            return
        }

        val payload = SubmitTaskPayload(
            task = s.task.trim(),
            description = s.description.trim().ifBlank { null },
            project = s.project.trim(),
            priority = s.priority,
            dateDue = s.dateDue,
            expectedOutcomes = s.expectedOutcomes.trim().ifBlank { null }
        )

        _state.value = s.copy(submitting = true, validationErrors = emptyMap(), errorTitle = null, errorBody = null)
        scope.launch {
            taskRepository.clearOutcome()
            val localId = taskRepository.submit(payload)
            taskRepository.trySync()
            val outcome = taskRepository.lastSubmitOutcome.value
            if (outcome != null && outcome.localId == localId && !outcome.success) {
                val (title, body) = ApiErrorMessages.forSubmit(outcome.errorCode, outcome.errorMessage)
                _state.value = _state.value.copy(submitting = false, errorTitle = title, errorBody = body)
                // Discard the optimistic entry on hard failure so the user can correct and retry
                if (outcome.errorCode == "not_provisioned" || outcome.errorCode == "rest_forbidden") {
                    taskRepository.discardLocal(localId)
                }
            } else {
                _state.value = _state.value.copy(submitting = false, submitted = true)
            }
        }
    }
}
