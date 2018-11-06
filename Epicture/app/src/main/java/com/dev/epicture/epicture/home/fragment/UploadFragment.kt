package com.dev.epicture.epicture.home.fragment

import android.animation.Animator
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.dev.epicture.epicture.MyApplication
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.imgur.service.ImgurService
import android.animation.AnimatorListenerAdapter
import android.util.Log


class UploadFragment : Fragment() {

    private val PICK_IMAGE = 1
    private var fragView: View? = null
    private var bitmap: Bitmap? = null
    private var bitmapName: String = ""

    private fun choose() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
    }

    private fun resetForm() {

        val title = fragView?.findViewById<EditText>(R.id.editTextTitle)
        val desc = fragView?.findViewById<EditText>(R.id.editTextDesc)
        val card = fragView?.findViewById<CardView>(R.id.choose_card)
        val preview = fragView?.findViewById<ImageView>(R.id.imagePreview)

        title?.text?.clear()
        desc?.text?.clear()
        bitmapName = ""
        card?.visibility = View.VISIBLE
        preview?.animate()
            ?.translationY(-preview.height.toFloat())
            ?.alpha(0.0f)
            ?.setDuration(300)
            ?.setListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {

                    super.onAnimationEnd(animation)
                    preview.setImageBitmap(null)
                    preview.y = preview.y + preview.height.toFloat()
                    preview.alpha = 1.0f


                }
            })
    }

    private fun upload() {
        val title = fragView?.findViewById<EditText>(R.id.editTextTitle)?.text.toString()
        val desc = fragView?.findViewById<EditText>(R.id.editTextDesc)?.text.toString()

        val context = activity

        if (title.isEmpty() || desc.isEmpty() || bitmapName.isEmpty() || bitmap == null) {
            return MyApplication.printMessage("Missing fields")
        }

        ImgurService.uploadImage({
            context?.runOnUiThread {
                MyApplication.printMessage("Upload of $title succeed")
            }
        },{
            context?.runOnUiThread {
                MyApplication.printMessage("Upload of $title failed")
            }
        }, bitmapName, title, desc, bitmap!!)

        resetForm()
    }

    private fun changeBitmap(uri: Uri?) {
        val imageView=  fragView?.findViewById<ImageView>(R.id.imagePreview)
        bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri)
        bitmapName = uri?.path!!
        imageView?.setImageBitmap(bitmap)
        imageView?.setOnClickListener {
            choose()
        }
        fragView?.findViewById<CardView>(R.id.choose_card)?.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE) {
            val imageUri = data?.data
            changeBitmap(imageUri)
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
