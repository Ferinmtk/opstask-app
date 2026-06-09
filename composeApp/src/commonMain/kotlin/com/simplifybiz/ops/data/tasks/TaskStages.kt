package com.simplifybiz.ops.data.tasks

/**
 * Workflow stage values (field 70 ~ STAGE on form 172). Used by mobile
 * filter chips on the tasks list. These must match the exact strings
 * the Gravity Flow workflow writes to the field.
 */
object TaskStages {
    const val ACCEPT = "0 Accept"
    const val WAITING = "1 Waiting"
    const val TO_DO = "1 To Do"
    const val DOING = "2 Doing"
    const val APPROVE = "3 Approve"
    const val FINAL_APPROVAL = "3 Final Approval"
    const val DONE = "4 Done"

    /**
     * Map mobile chip filters to the stage values they include.
     * "To do" includes Accept and Waiting (not-yet-started phases) since
     * from the client's perspective those are all "not yet being worked on".
     */
    val TO_DO_CHIP = setOf(ACCEPT, WAITING, TO_DO)
    val DOING_CHIP = setOf(DOING)
    val APPROVE_CHIP = setOf(APPROVE, FINAL_APPROVAL)
    val DONE_CHIP = setOf(DONE)
}
