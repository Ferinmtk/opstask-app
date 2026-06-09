package com.simplifybiz.ops.data.tasks

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire format for POST /tasks. Mirrors the v0.3.5 plugin contract:
 * - project: free text (mobile no longer picks from a list)
 * - priority: "1 Low", "2 Normal", "3 High"
 * - date_due: MM/DD/YYYY
 * - assigned_to and expected_outcomes are optional
 *
 * The plugin internally fills field 33 representativesEmail, field 40
 * billClientEmail, field 5 organization, field 82 search for client, and
 * field 84 lookupClientOrg from the authenticated user's profile and
 * form 177 lookup. Mobile does NOT send any of those.
 */
@Serializable
data class SubmitTaskPayload(
    val task: String,
    val description: String? = null,
    val project: String = "",
    val priority: String = Priorities.NORMAL,
    @SerialName("date_due") val dateDue: String,
    @SerialName("assigned_to") val assignedTo: String = "",
    @SerialName("expected_outcomes") val expectedOutcomes: String? = null
)
