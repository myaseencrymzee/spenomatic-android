package com.crymzee.spenomatic.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import javax.inject.Singleton





@Singleton
class Common internal constructor() {
    val intent = Intent()

    fun showToast(context: Context?, msg: String?) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }




    fun callIntentWithResult(context: Context, className: Class<*>, requestCode: Int) {
        val intent = Intent()
        intent.setClassName(context.packageName, className.canonicalName)
        (context as Activity).startActivityForResult(intent, requestCode)
    }



    fun restartActivity(activity: Activity,intent: Intent) {
       activity.finish()
       activity.overridePendingTransition( 0, 0);
       activity.startActivity(intent)
       activity.overridePendingTransition( 0, 0);

    }




    fun hideKeyboard(context: Context, editText: EditText) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0)
    }

    fun hideKeyboard(context: Context, editText: TextView) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0)
    }


    companion object {
        private var myInstance: Common? = null
        val instance: Common?
            get() {
                if (myInstance == null) {
                    myInstance = Common()
                }
                return myInstance
            }
    }
}