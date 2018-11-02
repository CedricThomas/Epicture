package com.dev.epicture.epicture

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.dev.epicture.epicture.imgur.service.ImgurService

class LoginActivity : AppCompatActivity() {

    private var button : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button = findViewById(R.id.button)
        button?.setOnClickListener {
            ImgurService.authorize(this)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        when (intent?.action) {
            Intent.ACTION_VIEW -> {
                ImgurService.registerCallbackInformations(intent)
                val newintent = Intent(this, GalleryActivity::class.java)
                startActivity(newintent)
            }
        }
    }
}
