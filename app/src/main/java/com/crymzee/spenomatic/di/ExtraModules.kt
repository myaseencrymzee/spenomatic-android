package com.crymzee.spenomatic.di


import androidx.navigation.NavOptions
import com.crymzee.spenomatic.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ExtraModules {

    @Singleton
    @Provides
    fun provideNavigationOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.enter_anim)
            .setExitAnim(R.anim.exit_anim)
            .setPopEnterAnim(R.anim.enter_from_left)
            .setPopExitAnim(R.anim.exit_to_right).build()
    }
}