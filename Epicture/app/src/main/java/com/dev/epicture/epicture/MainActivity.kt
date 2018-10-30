package com.dev.epicture.epicture

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.dev.epicture.epicture.imgur.service.ImgurService

class MainActivity : AppCompatActivity() {

    private var service : ImgurService? = null
    private var button : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button = findViewById(R.id.button)
        service = ImgurService("8aed2fba5dbcfe3", "1447955c839e7760fb3f75c36a994f0b57e8bc97%")
        button!!.setOnClickListener { _ ->
            service!!.login(this)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        when (intent!!.action) {
            Intent.ACTION_VIEW -> {
                val text = "Imgur Authentication Callback"
                val duration = Toast.LENGTH_SHORT

                val toast = Toast.makeText(applicationContext, text, duration)
                toast.show()
            }
        }
    }

}
