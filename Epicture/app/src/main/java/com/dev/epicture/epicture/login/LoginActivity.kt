package com.dev.epicture.epicture.login

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation
import android.widget.Button
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.home.HomeActivity
import com.dev.epicture.epicture.imgur.service.ImgurService


class LoginActivity : AppCompatActivity() {

    // Setup the first connection
    private fun setupFirstConnection() {

        val button = findViewById<Button>(R.id.button)

        button?.setOnClickListener {
            // Connect to Imgur and wait hook in this view
            ImgurService.askCredentials(this)

            // Card animation
            val scaleAnimation = ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f, Animation.RELATIVE_TO_SELF, 0.7f, Animation.RELATIVE_TO_SELF, 0.7f)
            scaleAnimation.duration = 500
            val bounceInterpolator = BounceInterpolator()
            scaleAnimation.interpolator = bounceInterpolator
            findViewById<CardView>(R.id.card).startAnimation(scaleAnimation)
        }

    }

    // Change activity
    private fun galleryAccess() {
        val newIntent = Intent(this, HomeActivity::class.java)
        startActivity(newIntent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ImgurService.loadCredentials({
            galleryAccess()
        }, {
            setupFirstConnection()
        })
    }

    // Connect to Imgur and wait hook in this view
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        when (intent?.action) {
            Intent.ACTION_VIEW -> {
                ImgurService.registerCredentials(intent)
                galleryAccess()
            }
        }
    }
}
