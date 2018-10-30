package com.dev.epicture.epicture

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.dev.epicture.epicture.imgur.service.ImgurService
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.widget.ImageView


class MainActivity : AppCompatActivity() {

    private var imgurService : ImgurService? = null
    private var button : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button = findViewById(R.id.button)
        imgurService = ImgurService(Config.clientID, Config.clientSecret)
        button!!.setOnClickListener {
            imgurService!!.authorize(this)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        when (intent?.action) {
            Intent.ACTION_VIEW -> {
                imgurService?.registerCallbackInformations(intent)
                imgurService?.getImages({resp ->
                    downloadImageTask(findViewById(R.id.imageView)).execute(resp.data[0].link)
                }, {e ->
                    Log.i("ImgurService", e.message)
                })
            }
        }
    }


    private inner class downloadImageTask(internal var bmImage: ImageView) : AsyncTask<String, Void, Bitmap>() {

        override fun doInBackground(vararg urls: String): Bitmap? {
            val urldisplay = urls[0]
            var mIcon11: Bitmap? = null
            try {
                val `in` = java.net.URL(urldisplay).openStream()
                mIcon11 = BitmapFactory.decodeStream(`in`)
            } catch (e: Exception) {
                Log.e("Error", e.message)
                e.printStackTrace()
            }

            return mIcon11
        }

        override fun onPostExecute(result: Bitmap) {
            bmImage.setImageBitmap(result)
        }
    }

}
