package com.crymzee.spenomatic.utils

import com.bumptech.glide.Priority
import com.crymzee.spenomatic.BuildConfig


open class Constants {
    companion object {

        var PERMISSION_LOCATION = 1000
        val imagePriority = Priority.LOW

        val SHARED_PREFS_NAME = BuildConfig.APPLICATION_ID
        val ACCESS_TOKEN = "user_access_token"

        val USER_ID = "user_id"

        val TUTORIAL_STATUS = "tutorial_status"
        val LAST_DESTINATION = "last_destination"
        val FILE_TYPE: String = "FILE_TYPE"
        val FIRST_TIME = "firs_time"
        val DEVICE_ID = "device_id"
        val FCM_TOKEN = "fcm_token"

        val REFRESH_TOKEN = "user_refresh_token"
        val USER_ROLE = "user_role"
        val PHONE_NUMBER = "phone_number"
        val USER_IMAGE = "user_image"
        val STATUS = "status"
        val NAME = "name"
        val USER_EMAIL = "user_email"

        val CHECKED_IN_USER = "checked_in_user"
        val CHECKED_IN_TIME = "checked_in_time"


    }
}