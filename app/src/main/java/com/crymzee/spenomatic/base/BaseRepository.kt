package com.crymzee.spenomatic.base


import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject

open class BaseRepository {

    fun convertErrorBody(response: ResponseBody): String {
        var res: String? = response.string()
        var jsonObject: JSONObject? = null
        var json: JSONObject? = null
        try {
            jsonObject = JSONObject(res)
            json = jsonObject.getJSONObject("errors")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val iter = json!!.keys()
        while (iter.hasNext()) {
            val key = iter.next()
            try {
                val jsonArray = json.getJSONArray(key)
                res = if (jsonArray.length() > 0) {
                    jsonArray[0].toString()
                } else {
                    ""
                }
            } catch (e: JSONException) {
            }
        }
        return res!!
    }
}

