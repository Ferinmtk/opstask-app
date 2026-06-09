package com.simplifybiz.ops.data.tasks

object TaskStatuses {
    const val TO_DO = "1 TO DO"
    const val DOING = "2 DOING"
    const val APPROVE = "3 APPROVE"
    const val DONE = "4 DONE"

    data class Filter(val label: String, val value: String?)

    val ALL_FILTERS = listOf(
        Filter("All", null),
        Filter("To do", TO_DO),
        Filter("Doing", DOING),
        Filter("Approve", APPROVE),
        Filter("Done", DONE)
    )
}
