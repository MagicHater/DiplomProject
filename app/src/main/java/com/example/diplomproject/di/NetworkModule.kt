package com.example.diplomproject.di

<<<<<<< codex/create-android-app-skeleton-with-compose-and-mvvm-fi3gre
import com.example.diplomproject.data.remote.AppApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
=======
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.example.diplomproject.data.remote.AppApi
>>>>>>> master
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
<<<<<<< codex/create-android-app-skeleton-with-compose-and-mvvm-fi3gre
import okhttp3.MediaType
=======
import okhttp3.MediaType.Companion.toMediaType
>>>>>>> master
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
<<<<<<< codex/create-android-app-skeleton-with-compose-and-mvvm-fi3gre
        val contentType = requireNotNull(MediaType.parse("application/json"))

        return Retrofit.Builder()
            .baseUrl("https://example.com/")
            .addConverterFactory(json.asConverterFactory(contentType))
=======
        return Retrofit.Builder()
            .baseUrl("https://example.com/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
>>>>>>> master
            .build()
    }

    @Provides
    @Singleton
    fun provideAppApi(retrofit: Retrofit): AppApi = retrofit.create(AppApi::class.java)
}
