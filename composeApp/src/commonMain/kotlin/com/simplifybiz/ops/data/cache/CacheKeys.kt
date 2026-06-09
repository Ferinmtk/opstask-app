package com.simplifybiz.ops.data.cache

object CacheKeys {
    const val TASKS = "cache_tasks_v1"
    const val PROJECTS = "cache_projects_v1"
    const val ASSIGNEES = "cache_assignees_v1"
    const val MESSAGES = "cache_messages_v1"
    const val LAST_SYNC = "cache_last_sync"

    fun comments(taskId: Int) = "cache_comments_${taskId}_v1"
    fun taskDetail(id: Int) = "cache_task_${id}_v1"
}
