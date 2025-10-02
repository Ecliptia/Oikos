package com.ecliptia.oikos.di

import com.ecliptia.oikos.data.repository.OikosRepository
import com.ecliptia.oikos.data.repository.OikosRepositoryImpl
import com.google.firebase.database.FirebaseDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindOikosRepository(impl: OikosRepositoryImpl): OikosRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseDatabase(): FirebaseDatabase {
            return FirebaseDatabase.getInstance()
        }

        @Provides
        @Singleton
        fun provideFirebaseAuth(): com.google.firebase.auth.FirebaseAuth {
            return com.google.firebase.auth.FirebaseAuth.getInstance()
        }

        @Provides
        @Singleton
        fun provideGeminiAiService(): com.ecliptia.oikos.services.GeminiAiService {
            return com.ecliptia.oikos.services.GeminiAiService()
        }
    }
}