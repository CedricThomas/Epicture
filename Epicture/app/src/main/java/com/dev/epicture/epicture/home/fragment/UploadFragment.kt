package com.dev.epicture.epicture.home.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.imgur.service.ImgurService

class UploadFragment : Fragment() {

    private val PICK_IMAGE = 1
    private var fragView: View? = null
    private var bitmap: Bitmap? = null
    private var bitmapName: String = ""

    fun choose() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
    }

    fun upload() {
        val title = fragView?.findViewById<EditText>(R.id.editTextTitle)!!.text
        val desc = fragView?.findViewById<EditText>(R.id.editTextDesc)!!.text
        if (title.isEmpty() || desc.isEmpty() || bitmapName.isEmpty() || bitmap == null) {
            Log.e("Error Upload", "Fill every field")
            return
        }
        ImgurService.uploadImage({it ->
            Log.i("Upload", it.asString)
        },{it ->
            Log.i("UploadError", it.message)
        }, bitmapName, title.toString(), desc.toString(), bitmap!!)
    }

    private fun changeBitmap(uri: Uri) {
        val imageView=  fragView?.findViewById<ImageView>(R.id.imagePreview)
        bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri)
        bitmapName = uri.path!!
        imageView?.setImageBitmap(bitmap)
        imageView?.setOnClickListener {
            choose()
        }
        fragView?.findViewById<CardView>(R.id.choose_card)?.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE) {
            val imageUri = data?.data
            changeBitmap(imageUri!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragView = inflater.inflate(R.layout.fragment_upload, container, false)
        val chooseButton = fragView?.findViewById<Button>(R.id.buttonChoose)
        chooseButton?.setOnClickListener {
            choose()
        }
        val uploadButton = fragView?.findViewById<Button>(R.id.buttonUpload)
        uploadButton?.setOnClickListener {
            upload()
        }
        return fragView
    }

}
