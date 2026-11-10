package com.erns.alertauni.data.di

import com.erns.alertauni.BuildConfig
import com.erns.alertauni.data.demo.DemoAnnounceRepository
import com.erns.alertauni.data.demo.DemoCommentRepository
import com.erns.alertauni.data.demo.DemoContactRepository
import com.erns.alertauni.data.demo.DemoCourseRepository
import com.erns.alertauni.data.demo.DemoPostRepository
import com.erns.alertauni.data.demo.DemoStudentRepository
import com.erns.alertauni.data.repository.AccountRepository
import com.erns.alertauni.data.repository.AccountRepositoryImpl
import com.erns.alertauni.data.repository.AnnounceRepository
import com.erns.alertauni.data.repository.AnnounceRepositoryImpl
import com.erns.alertauni.data.repository.AuthRepository
import com.erns.alertauni.data.repository.AuthRepositoryImpl
import com.erns.alertauni.data.repository.CommentRepository
import com.erns.alertauni.data.repository.CommentRepositoryImpl
import com.erns.alertauni.data.repository.ContactRepository
import com.erns.alertauni.data.repository.ContactRepositoryImpl
import com.erns.alertauni.data.repository.CourseRepository
import com.erns.alertauni.data.repository.CourseRepositoryImpl
import com.erns.alertauni.data.repository.PostRepository
import com.erns.alertauni.data.repository.PostRepositoryImpl
import com.erns.alertauni.data.repository.StudentRepository
import com.erns.alertauni.data.repository.StudentRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(
        accountRepositoryImpl: AccountRepositoryImpl
    ): AccountRepository
}

/**
 * Elige, para los repositorios que usa el Muro de Anuncios, la implementación real (Supabase)
 * o la SIMULADA del modo demo. BuildConfig.DEMO_MODE solo puede ser true en debug.
 * Se usa Provider<> para no construir la implementación que no se usa.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositorySelectorModule {

    private val demo get() = BuildConfig.DEMO_MODE

    @Provides
    @Singleton
    fun providePostRepository(
        real: Provider<PostRepositoryImpl>, simulado: Provider<DemoPostRepository>
    ): PostRepository = if (demo) simulado.get() else real.get()

    @Provides
    @Singleton
    fun provideCommentRepository(
        real: Provider<CommentRepositoryImpl>, simulado: Provider<DemoCommentRepository>
    ): CommentRepository = if (demo) simulado.get() else real.get()

    @Provides
    @Singleton
    fun provideContactRepository(
        real: Provider<ContactRepositoryImpl>, simulado: Provider<DemoContactRepository>
    ): ContactRepository = if (demo) simulado.get() else real.get()

    @Provides
    @Singleton
    fun provideStudentRepository(
        real: Provider<StudentRepositoryImpl>, simulado: Provider<DemoStudentRepository>
    ): StudentRepository = if (demo) simulado.get() else real.get()

    @Provides
    @Singleton
    fun provideCourseRepository(
        real: Provider<CourseRepositoryImpl>, simulado: Provider<DemoCourseRepository>
    ): CourseRepository = if (demo) simulado.get() else real.get()

    @Provides
    @Singleton
    fun provideAnnounceRepository(
        real: Provider<AnnounceRepositoryImpl>, simulado: Provider<DemoAnnounceRepository>
    ): AnnounceRepository = if (demo) simulado.get() else real.get()
}
