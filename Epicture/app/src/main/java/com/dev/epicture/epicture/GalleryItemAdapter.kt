package com.dev.epicture.epicture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.dev.epicture.epicture.imgur.service.ImgurService
import kotlinx.android.synthetic.main.recycler_view_item.view.*

class GalleryItemAdapter(private val items : ArrayList<String>, private val context: Context) : RecyclerView.Adapter<GalleryItemAdapter.ViewHolder>() {

    inner class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val textView = view.imageView!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_view_item, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        ImgurService.getImages(0, { resp ->
            try {
                if (position < resp.data.size)
                    DownloadImageTask(holder.textView).execute(resp.data[position].link)
            } catch (e : Exception) {
                Log.i("ImgurService", e.message)
            }
        }, {e ->
            Log.i("ImgurService", e.message)
        })
    }

    private inner class DownloadImageTask(internal var bmImage: ImageView) : AsyncTask<String, Void, Bitmap>() {

        override fun doInBackground(vararg urls: String): Bitmap? {
            val urldisplay = urls[0]
            var image: Bitmap? = null

            try {
                val stream = java.net.URL(urldisplay).openStream()
                image = BitmapFactory.decodeStream(stream)
            } catch (e: Exception) {
                Log.e("Error", e.message)
                e.printStackTrace()
            }

            return image
        }

        override fun onPostExecute(result: Bitmap) {
            bmImage.setImageBitmap(result)
        }
    }

}

