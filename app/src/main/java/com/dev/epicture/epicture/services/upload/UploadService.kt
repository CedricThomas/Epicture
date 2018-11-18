package com.dev.epicture.epicture.services.upload

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.content.ContextCompat.startActivity
import com.dev.epicture.epicture.MyApplication
import com.dev.epicture.epicture.activities.upload.UploadActivity

object UploadService {

    private val PICK_IMAGE = 1
    private val CAMERA_IMAGE = 2

    /**
     * Start a gallery selection intent with tag PICK_IMAGE
     *
     *  @param activity : Activity catching the selection result
     */
    fun choose(activity: Activity) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(activity, Intent.createChooser(intent, "Select Picture"), PICK_IMAGE, null)
    }

    /**
     * Start a camera capture intent with tag CAMERA_IMAGE
     *
     *  @param activity : Activity catching the selection result
     */
    fun camera(activity: Activity) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(activity.packageManager)?.also {
                startActivityForResult(activity, takePictureIntent, CAMERA_IMAGE, null)
            }
        }
    }

    /**
     * Launch the uploadActivity with the filename needed to be upload as parameter
     *
     *  @param context : Base context of intent creation
     *  @param filename : Name of the file to upload stored in the stockage
     */
    private fun startNewUploadActivity(context: Context, filename: String) {

        val intent = Intent(context, UploadActivity::class.java)
        intent.putExtra("image", filename);
        startActivity(context, intent, null)
    }

    /**
     * start the upload of an image by opening the uploadActivity
     *
     *  @param context : Base context of intent creation
     *  @param imageBitmap : Image to upload
     */
    private fun startUpload(context: Context, imageBitmap : Bitmap) {

        //Write file
        val filename = "bitmap.png"
        val stream = MyApplication.appContext?.openFileOutput(filename, Context.MODE_PRIVATE)
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

        //Cleanup
        stream?.close()
        imageBitmap.recycle()
        startNewUploadActivity(context, filename)
    }

    /**
     * Overload of onActivityResult of the calling Activity triggering upload
     *
     */
    fun onActivityResult(context: Context, requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK)
            return
        when (requestCode) {
            PICK_IMAGE -> {
                val imageUri = data?.data
                val imageBitmap = MediaStore.Images.Media.getBitmap(MyApplication.appContext?.contentResolver, imageUri)
                startUpload(context, imageBitmap)
            }
            CAMERA_IMAGE -> {
                val imageBitmap = data?.extras?.get("data") as Bitmap
                startUpload(context, imageBitmap)
            }
        }
    }

}