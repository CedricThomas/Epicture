package com.dev.epicture.epicture.imgur.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.content.ContextCompat.startActivity

class ImgurService(val clientId: String, val clientSecret: String) {

    fun login(context: Context) {
        val url = "https://api.imgur.com/oauth2/authorize?client_id=$clientId&response_type=token"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(context, intent, null)
    }
}