package com.crymzee.spenomatic.retrofit

import com.crymzee.spenomatic.model.request.RefreshTokenRequestBody
import com.crymzee.spenomatic.model.response.RefreshResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenRefreshApi {
    @POST("auth/token/refresh")
    suspend fun refreshToken(@Body refreshTokenRequestBody: RefreshTokenRequestBody): Response<RefreshResponseBody>
}



