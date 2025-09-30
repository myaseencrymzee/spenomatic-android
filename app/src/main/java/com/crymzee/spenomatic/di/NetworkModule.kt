package com.crymzee.spenomatic.di

import android.content.Context
import com.crymzee.spenomatic.BuildConfig
import com.crymzee.spenomatic.connectivity.base.ConnectivityProvider
import com.crymzee.spenomatic.retrofit.ApiServices
import com.crymzee.spenomatic.sharedPreference.SharedPrefsHelper
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideConnectivityModule(@ApplicationContext context: Context): ConnectivityProvider {
        return ConnectivityProvider.createProvider(context)
    }

    @Singleton
    @Provides
    fun provideAuthInterceptor(sharedPrefsHelper: SharedPrefsHelper): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()

            val token = sharedPrefsHelper.getUserAuth()
            if (!token.isNullOrBlank()) {
                SpenoMaticLogger.logErrorMsg("Auth Token", "Present ✅")
                requestBuilder.addHeader("Authorization", "Bearer $token")
            } else {
                SpenoMaticLogger.logErrorMsg("Auth Token", "Missing ❌")
            }

            requestBuilder.addHeader("cache-control", "no-cache")
            chain.proceed(requestBuilder.build())
        }
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(
        authInterceptor: Interceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("${BuildConfig.BASE_URL}")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideRemoteDataSource(retrofit: Retrofit): RemoteDataSource {
        return RemoteDataSource(retrofit)
    }

    @Singleton
    @Provides
    fun provideApiServices(remoteDataSource: RemoteDataSource): ApiServices {
        return remoteDataSource.buildApi(ApiServices::class.java)
    }
}

