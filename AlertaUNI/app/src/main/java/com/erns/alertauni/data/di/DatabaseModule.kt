package com.erns.alertauni.data.di

import android.content.Context
import com.erns.alertauni.data.local.AppDatabase
import com.erns.alertauni.data.local.PostExtraDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Misma instancia que usan NotificacionesViewModel y MiFirebaseService.
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    fun providePostExtraDao(db: AppDatabase): PostExtraDao = db.postExtraDao()
}
