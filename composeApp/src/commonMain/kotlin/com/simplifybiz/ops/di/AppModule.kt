package com.simplifybiz.ops.di

import com.russhwolf.settings.Settings
import com.simplifybiz.ops.data.SecureStorage
import com.simplifybiz.ops.data.SessionManager
import com.simplifybiz.ops.data.auth.AuthRepository
import com.simplifybiz.ops.data.cache.JsonCache
import com.simplifybiz.ops.data.cache.PendingQueue
import com.simplifybiz.ops.data.comments.CommentRepository
import com.simplifybiz.ops.data.messages.MessageRepository
import com.simplifybiz.ops.data.tasks.TaskRepository
import com.simplifybiz.ops.presentation.login.LoginViewModel
import com.simplifybiz.ops.presentation.messages.MessagesViewModel
import com.simplifybiz.ops.presentation.tasks.SubmitTaskViewModel
import com.simplifybiz.ops.presentation.tasks.TaskDetailViewModel
import com.simplifybiz.ops.presentation.tasks.TasksListViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * platformModule is provided by each platform (Android, iOS) and supplies:
 *   - SecureStorage (Android Keystore-backed or iOS Keychain)
 *   - Settings (multiplatform-settings, manually constructed per platform)
 *
 * appModule consumes both via `get<SecureStorage>()` and `get<Settings>()`.
 */
expect val platformModule: Module

val appModule = module {
    single { SessionManager(get(), get<SecureStorage>()) }
    single { JsonCache(get(), get()) }
    single { PendingQueue(get()) }

    single { AuthRepository(get(), get(), get()) }
    single { TaskRepository(get(), get(), get(), get(), get()) }
    single { CommentRepository(get(), get()) }
    single { MessageRepository(get(), get(), get()) }

    factoryOf(::LoginViewModel)
    factoryOf(::TasksListViewModel)
    factoryOf(::SubmitTaskViewModel)
    factoryOf(::TaskDetailViewModel)
    factoryOf(::MessagesViewModel)
}
