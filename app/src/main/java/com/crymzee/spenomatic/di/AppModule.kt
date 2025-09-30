package com.crymzee.spenomatic.di

import com.crymzee.spenomatic.sharedPreference.SharedPrefsHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideSharedPrefsHelper(): SharedPrefsHelper {
        return SharedPrefsHelper // Return the object reference
    }
}
