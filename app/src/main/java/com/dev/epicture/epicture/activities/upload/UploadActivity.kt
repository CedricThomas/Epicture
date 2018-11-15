package com.dev.epicture.epicture.activities.upload

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.dev.epicture.epicture.R
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.dev.epicture.epicture.MyApplication
import com.dev.epicture.epicture.services.imgur.ImgurService
import kotlinx.android.synthetic.main.activity_upload.*
import android.graphics.BitmapFactory




class UploadActivity : AppCompatActivity() {

    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        val filename = intent.getStringExtra("image")
        val stream = this.openFileInput(filename)
        bitmap = BitmapFactory.decodeStream(stream)
        imageView.setImageBitmap(bitmap)
        stream.close()
        button.setOnClickListener {
            if (descriptionText.text.isEmpty() || titleText.text.isEmpty()) {
                return@setOnClickListener Toast.makeText(this, "Field are missing", Toast.LENGTH_SHORT).show()
            }
            val title = titleText.text.toString()
            val description = descriptionText.text.toString()
            ImgurService.uploadImage({_ ->
                Toast.makeText(baseContext, "Upload of $title succeed", Toast.LENGTH_SHORT).show() // TODO fix
            }, {_ ->
                Toast.makeText(baseContext, "Upload of $title failed", Toast.LENGTH_SHORT).show()
            },
                "upload",
                title,
                description,
                bitmap)
            super.onBackPressed()
        }
    }
}
