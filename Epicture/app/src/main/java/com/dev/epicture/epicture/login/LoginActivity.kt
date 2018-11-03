package com.dev.epicture.epicture.login

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.home.HomeActivity
import com.dev.epicture.epicture.imgur.service.ImgurService
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration

class LoginActivity : AppCompatActivity() {

    private var button : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create global configuration and initialize ImageLoader with this config
        val config = ImageLoaderConfiguration.Builder(this).build()
        ImageLoader.getInstance().init(config)
        setContentView(R.layout.activity_login)
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
                val newIntent = Intent(this, HomeActivity::class.java)
                startActivity(newIntent)
                finish()
            }
        }
    }
}
