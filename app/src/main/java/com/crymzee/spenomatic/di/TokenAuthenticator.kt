package com.crymzee.spenomatic.di

import com.crymzee.spenomatic.model.request.RefreshTokenRequestBody
import com.crymzee.spenomatic.retrofit.TokenRefreshApi
import com.crymzee.spenomatic.sharedPreference.SharedPrefsHelper
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val tokenRefreshApi: TokenRefreshApi,
    private val sharedPrefsHelper: SharedPrefsHelper
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        val newToken = runBlocking {
            refreshToken()
        } ?: return null

        sharedPrefsHelper.setUserAuth(newToken)

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var res = response.priorResponse
        var count = 1
        while (res != null) {
            count++
            res = res.priorResponse
        }
        return count
    }

    private suspend fun refreshToken(): String? {
        return try {
            val refreshToken = sharedPrefsHelper.getUserAuth()
            SpenoMaticLogger.logDebugMsg("RefreshTokenCheck", "refreshToken = $refreshToken")

            val response = tokenRefreshApi.refreshToken(RefreshTokenRequestBody(refreshToken ?: ""))

            SpenoMaticLogger.logDebugMsg("RefreshTokenResponse", "Success: ${response.isSuccessful}, Code: ${response.code()}, Body: ${response.body()}, Error: ${response.errorBody()?.string()}")

            if (response.isSuccessful && response.body() != null) {
                val accessToken = response.body()!!.access
                SpenoMaticLogger.logDebugMsg("RefreshTokenBody", "Access token: $accessToken")
                accessToken
            } else {
                null
            }
        } catch (e: Exception) {
            SpenoMaticLogger.logErrorMsg("RefreshTokenError", e.message ?: "Unknown error")
            null
        }
    }

}
