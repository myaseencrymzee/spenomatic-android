package com.crymzee.spenomatic.utils

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class PlaceApi {

    fun autoComplete(input: String): ArrayList<String> {
        val arrayList = ArrayList<String>()
        var connection: HttpURLConnection? = null
        val jsonResult = StringBuilder()

        try {
            val url = URL("https://maps.googleapis.com/maps/api/place/autocomplete/json?input=$input&key=AIzaSyCzPwpj2KUbaLOuMVmok3NUPObyFmdmUZg")
            connection = url.openConnection() as HttpURLConnection

            InputStreamReader(connection.inputStream).use { reader ->
                val buff = CharArray(1024)
                var read: Int
                while (reader.read(buff).also { read = it } != -1) {
                    jsonResult.append(buff, 0, read)
                }
            }

            Log.d("JSon", jsonResult.toString())

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
        }

        try {
            val jsonObject = JSONObject(jsonResult.toString())
            val predictions: JSONArray = jsonObject.getJSONArray("predictions")
            for (i in 0 until predictions.length()) {
                arrayList.add(predictions.getJSONObject(i).getString("description"))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return arrayList
    }
}
