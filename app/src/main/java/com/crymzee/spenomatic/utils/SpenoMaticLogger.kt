package com.crymzee.spenomatic.utils

import android.util.Log
import com.crymzee.spenomatic.BuildConfig


object SpenoMaticLogger {

    /**
     * Method for showing the debug log messages
     *
     * @param TAG
     * @param message
     */
    fun logDebugMsg(TAG: String, message: String?) {

        if (BuildConfig.DEBUG) {
            message?.let {
                Log.d(TAG, it)
            }
        }

    }

    /**
     * Method for showing the all log messages
     *
     * @param TAG
     * @param message
     */
    fun logVerboseMsg(TAG: String, message: String?) {

        if (BuildConfig.DEBUG) {
            message?.let {
                Log.v(TAG, it)
            }
        }


    }

    /**
     * Method for showing the info log messages
     *
     * @param TAG
     * @param message
     */
    fun logInfoMsg(TAG: String, message: String?) {
        if (BuildConfig.DEBUG) {
            message?.let {
                Log.i(TAG, it)
            }
        }
    }

    /**
     * Method for showing the Log Error Messages
     *
     * @param TAG
     * @param message
     */
    fun logErrorMsg(TAG: String, message: String?) {

        if (BuildConfig.DEBUG) {
            message?.let {
                Log.e(TAG, it)
            }


        }


    }

    /**
     * Method for printing the exception message
     *
     * @param TAG
     */
    fun logException(TAG: String, e: Exception?) {
        e?.let {
            Log.e(TAG, Log.getStackTraceString(it))
        }


    }
}
