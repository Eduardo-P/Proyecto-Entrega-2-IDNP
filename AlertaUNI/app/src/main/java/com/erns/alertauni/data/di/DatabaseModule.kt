package com.erns.alertauni.data.di

import android.content.Context
import com.erns.alertauni.data.demo.DemoDao
import com.erns.alertauni.data.local.AppDatabase
import com.erns.alertauni.data.local.DraftDao
import com.erns.alertauni.data.local.PostExtraDao
import com.erns.alertauni.data.repository.DraftRepository
import com.erns.alertauni.data.repository.DraftRepositoryImpl
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

    @Provides
    fun provideDraftDao(db: AppDatabase): DraftDao = db.draftDao()

    // Borradores: almacenamiento local real (Room) tanto en modo real como en demo.
    @Provides
    @Singleton
    fun provideDraftRepository(impl: DraftRepositoryImpl): DraftRepository = impl

    // SIMULADO: solo lo usan los repositorios del modo demo.
    @Provides
    fun provideDemoDao(db: AppDatabase): DemoDao = db.demoDao()
}
