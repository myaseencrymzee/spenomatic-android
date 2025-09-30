package com.crymzee.spenomatic.di

import com.crymzee.spenomatic.base.BaseRepository
import com.crymzee.spenomatic.repository.AuthRepository
import com.crymzee.spenomatic.repository.CustomersRepository
import com.crymzee.spenomatic.repository.ExpensesRepository
import com.crymzee.spenomatic.repository.HomeRepository
import com.crymzee.spenomatic.repository.LeavesRepository
import com.crymzee.spenomatic.repository.VisitsRepository
import com.crymzee.spenomatic.retrofit.ApiServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun baseRepository(): BaseRepository {
        return baseRepository()
    }

    @Singleton
    @Provides
    fun authRepository(apiServices: ApiServices)
            : AuthRepository {
        return AuthRepository(apiServices)
    }

    @Singleton
    @Provides
    fun customersRepository(apiServices: ApiServices)
            : CustomersRepository {
        return CustomersRepository(apiServices)
    }
    @Singleton
    @Provides
    fun visitsRepository(apiServices: ApiServices)
            : VisitsRepository {
        return VisitsRepository(apiServices)
    }

    @Singleton
    @Provides
    fun leavesRepository(apiServices: ApiServices)
            : LeavesRepository {
        return LeavesRepository(apiServices)
    }


    @Singleton
    @Provides
    fun expensesRepository(apiServices: ApiServices)
            : ExpensesRepository {
        return ExpensesRepository(apiServices)
    }

    @Singleton
    @Provides
    fun homeRepository(apiServices: ApiServices)
            : HomeRepository {
        return HomeRepository(apiServices)
    }

}














