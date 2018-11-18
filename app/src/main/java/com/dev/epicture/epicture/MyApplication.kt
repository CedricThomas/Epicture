package com.dev.epicture.epicture

import android.app.Application
import android.content.Context
import android.widget.Toast


/**
 * static class allowing to access to the application context and toasting with  the application context
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {

        fun printMessage(message: String) {
            Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
        }

        /**
         * context of the Application
         */
        var appContext: Context? = null
            private set
    }

}