package com.crymzee.spenomatic.sharedPreference


import android.content.Context
import android.content.SharedPreferences
import com.crymzee.spenomatic.SpenoMaticApp
import com.crymzee.spenomatic.model.response.loginResponseBody.LoginResponseBody
import com.crymzee.spenomatic.utils.Constants

object SharedPrefsHelper {

    private var sharedPreferences: SharedPreferences =
        SpenoMaticApp.instance!!.getSharedPreferences(
            Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE
        )



    fun saveLoggedInInstitute(response: LoginResponseBody) {
        save(
            Constants.REFRESH_TOKEN,
            response.refresh
        )

        save(
            Constants.ACCESS_TOKEN,
            response.access
        )

    }


    fun getUserAuthRefresh(): String? = get<String?>(
        Constants.REFRESH_TOKEN,
        null
    )
    fun getFCMToken(): String? =
      get<String?>(
            Constants.FCM_TOKEN,
            null
        )



    fun getUserImage(): String? = get<String?>(
        Constants.USER_IMAGE,
        null
    )


    fun getStatus(): String? = get<String?>(
        Constants.STATUS,
        null
    )

    fun getUserCheckedIn(): Boolean? = get<Boolean?>(
        Constants.CHECKED_IN_USER,
        null
    )


    fun getUserCheckedInTime(): String? = get<String?>(
        Constants.CHECKED_IN_TIME,
        null
    )



    fun getUserCheckedInDate(): String? = get<String?>(
        Constants.LAST_CHECKIN_DATE,
        null
    )

    fun getUserCheckedInId(): String? = get<String?>(
        Constants.LAST_CHECKIN_ID,
        null
    )


    fun getUserId(): Int? = get<Int?>(
        Constants.USER_ID,
        null
    )

    fun getUserEmail(): String? = get<String?>(
        Constants.USER_EMAIL,
        null
    )

    fun getUserAuth(): String? = get<String?>(
        Constants.ACCESS_TOKEN,
        null
    )

    fun getUserName(): String? = get<String?>(
        Constants.NAME,
        null
    )


    fun setUserID(id: Int?) {
        save(Constants.USER_ID, id)
    }

    fun setUserCheckedIn(id: Boolean?) {
        save(Constants.CHECKED_IN_USER, id)
    }

    fun setUserCheckedInTime(id: String?) {
        save(Constants.CHECKED_IN_TIME, id)
    }


    fun setUserCheckedInTimeDate(id: String?) {
        save(Constants.LAST_CHECKIN_DATE, id)
    }


    fun setUserCheckedInId(id: String?) {
        save(Constants.LAST_CHECKIN_ID, id)
    }

    fun setStatus(id: String?) {
        save(Constants.STATUS, id)
    }

    fun setName(id: String?) {
        save(Constants.NAME, id)
    }

    fun setUserImage(id: String?) {
        save(Constants.USER_IMAGE, id)
    }


    fun delete(key: String) {
        if (sharedPreferences.contains(key)) {
            sharedPreferences.edit().remove(key).apply()
        }
    }

    fun setUserAuth(authToken: String?) {
        save(
            Constants.ACCESS_TOKEN,
            authToken
        )
    }

    fun setUserRefreshToken(refreshToken: String?) {
        save(
            Constants.REFRESH_TOKEN,
            refreshToken
        )
    }


    fun setPhoneNumber(firstName: String?) {
        save(
            Constants.PHONE_NUMBER,
            firstName
        )
    }

    fun setUserEmail(email: String?) {
        save(Constants.USER_EMAIL, email)
    }



    fun setUserRole(role: String?) {
        save(Constants.USER_ROLE, role)
    }




    fun setDeviceId(deviceId: String?) {
       save(Constants.DEVICE_ID, deviceId)
    }


    fun setFCMToken(token: String?) {
      save(Constants.FCM_TOKEN, token)
    }




    /**
     * Get a boolean value from SharedPreferences
     */
    fun getBool(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getBoolean(key, defaultValue)
    }

    fun save(key: String, value: Any?) {
        val editor = sharedPreferences.edit()
        when {
            value is Boolean -> editor.putBoolean(key, (value))
            value is Int -> editor.putInt(key, (value))
            value is Float -> editor.putFloat(key, (value))
            value is Long -> editor.putLong(key, (value))
            value is String -> editor.putString(key, value)
            value is Enum<*> -> editor.putString(key, value.toString())
            value != null -> throw RuntimeException("Attempting to save non-supported preference")
        }
        editor.apply()
    }

    /*
       GETTER METHODS
     */


    /*
       SETTER METHODS
     */

    fun clearAllData() {
        sharedPreferences.edit().clear().apply()

    }


    fun setCallFrom(value: Int) {
        save(Constants.LAST_DESTINATION, value)
    }


    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String): T {
        return sharedPreferences.all[key] as T
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String, defValue: T): T {
        val returnValue = sharedPreferences.all[key] as T
        return returnValue ?: defValue
    }

    fun has(key: String): Boolean {
        return sharedPreferences.contains(key)
    }


    /*Todo New Implementation*/


}