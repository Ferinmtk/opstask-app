package com.simplifybiz.ops.data.tasks

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Task entity as returned by the plugin GET /tasks and POST /tasks.
 * Mirrors TaskEntity::to_array() on the PHP side.
 *
 * Two status-like fields exist on form 172:
 *   - status: client-facing label set on submission (rarely changes)
 *   - stage:  workflow-driven, advances as Gravity Flow steps run
 *
 * The mobile filter chips on the tasks list filter by stage.
 */
@Serializable
data class Task(
    val id: Int = 0,
    val task: String = "",
    val description: String = "",
    val project: String = "",
    val priority: String = "",
    @SerialName("date_due") val dateDue: String = "",
    val status: String = "",
    val stage: String = "",
    @SerialName("stage_management") val stageManagement: String = "",
    @SerialName("assign_to") val assignTo: String = "",
    @SerialName("submitter_email") val submitterEmail: String = "",
    @SerialName("bill_client_email") val billClientEmail: String = "",
    val organization: String = "",
    @SerialName("expected_outcomes") val expectedOutcomes: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)
