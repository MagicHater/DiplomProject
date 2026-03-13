package com.example.diplomproject.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    @Provides
    @Singleton
    fun provideRetrofit(json: Json): Retrofit {
        val contentType = requireNotNull(okhttp3.MediaType.parse("application/json"))

        return Retrofit.Builder()
            .baseUrl("https://example.com/")
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideAppApi(retrofit: Retrofit): com.example.diplomproject.data.remote.AppApi {
        return retrofit.create(com.example.diplomproject.data.remote.AppApi::class.java)
    }
}
