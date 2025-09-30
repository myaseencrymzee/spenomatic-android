package com.crymzee.spenomatic.di

import retrofit2.Retrofit
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val retrofit: Retrofit
) {
    fun <Api> buildApi(api: Class<Api>): Api {
        return retrofit.create(api)
    }
}
