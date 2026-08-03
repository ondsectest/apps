package com.surestep.app.di

import javax.inject.Qualifier

/**
 * A [kotlinx.coroutines.CoroutineScope] that lives as long as the process.
 * Used for work that must finish even if the screen that started it goes away —
 * chiefly, writing a confirmation record.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
