package com.crymzee.spenomatic.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.crymzee.spenomatic.sharedPreference.SharedPrefsHelper
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.google.android.gms.location.LocationServices
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@HiltWorker
class TrackingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "TrackingWorker"
        private const val KEY_CHECKIN_ID = "checkin_id"
        private const val KEY_IS_FIRST_RUN = "is_first_run"

        fun scheduleOnce(
            context: Context,
            delayMinutes: Long,
            isFirstRun: Boolean = false,
            checkInId: String? = null
        ) {
            val data = workDataOf(
                KEY_CHECKIN_ID to (checkInId ?: SharedPrefsHelper.getUserCheckedInId()),
                KEY_IS_FIRST_RUN to isFirstRun
            )

            val request = OneTimeWorkRequestBuilder<TrackingWorker>()
                .setInputData(data)
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .addTag("tracking_${System.currentTimeMillis()}") // ✅ unique tag each time
                .build()

            Log.d(TAG, "📅 Scheduling tracking work (delay=$delayMinutes min, checkInId=$checkInId)")
            WorkManager.getInstance(context).enqueue(request)
        }



        fun cancelTracking(context: Context) {
            Log.d(TAG, "🛑 Cancelling tracking work")
            WorkManager.getInstance(context).cancelUniqueWork("user_tracking_work")
        }

        fun randomMinutes(min: Int, max: Int): Long = Random.nextInt(min, max + 1).toLong()
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "---- WorkManager started ----")

            val context = applicationContext
            val checkInId = inputData.getString(KEY_CHECKIN_ID)
                ?: SharedPrefsHelper.getUserCheckedInId()

            if (checkInId.isNullOrEmpty()) {
                Log.e(TAG, "❌ checkInId is missing — cannot continue")
                return@withContext Result.failure()
            }

            val token = SharedPrefsHelper.getUserAuth()
            if (token.isNullOrEmpty()) {
                Log.e(TAG, "❌ Token missing — cannot call API")
                return@withContext Result.failure()
            }

            // Check permissions
            val hasFine = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasFine && !hasCoarse) {
                Log.e(TAG, "❌ Location permission missing")
                return@withContext Result.failure()
            }

            // Get live location
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val location = fusedClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null
            ).await()

            if (location == null) {
                Log.e(TAG, "❌ Failed to get location (null)")
                return@withContext Result.retry()
            }

            val lat = location.latitude
            val lng = location.longitude
            Log.d(TAG, "📍 Current location: lat=$lat, lng=$lng")

            // Build JSON payload
            val json = JSONObject().apply {
                put("attendance", checkInId.toInt())

                val coordinates = org.json.JSONArray().apply {
                    put(lng.toDouble())  // longitude first
                    put(lat.toDouble())  // latitude second
                }

                val locationJson = JSONObject().apply {
                    put("type", "Point")
                    put("coordinates", coordinates)
                }

                put("location", locationJson)
            }

            // ✅ Log full request body clearly
            Log.d(TAG, "📤 Tracking API request body:\n${json.toString(2)}")

            // Build OkHttp request
            val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

            val request = Request.Builder()
                .url("https://spenomatic-api.dev.crymzee.com/api/users/tracking")
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $token")
                .post(body)
                .build()

            // Execute request
            val client = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                Log.d(TAG, "✅ Tracking API success for checkInId=$checkInId")
                response.close()

                val nextDelay = randomMinutes(1, 15)
                scheduleOnce(context, nextDelay, false, checkInId)
                Log.d(TAG, "⏱ Next tracking scheduled after $nextDelay minutes")

                return@withContext Result.success()
            } else {
                val error = response.body?.string()
                Log.e(TAG, "❌ Tracking API failed: $error")
                Log.e(TAG, "❌ Request payload was:\n${json.toString(2)}")
                response.close()
                return@withContext Result.retry()
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Worker exception: ${e.message}")
            e.printStackTrace()
            return@withContext Result.retry()
        }
    }
}
