package com.dev.epicture.epicture.home

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.imgur.service.models.ImageModel
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.recycler_view_item.view.*

class GalleryItemAdapter(private val images : ArrayList<ImageModel>, private val context: Context) : RecyclerView.Adapter<GalleryItemAdapter.ImageHolder>() {


    inner class ImageHolder (view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.imageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ImageHolder {
        val inflatedView = LayoutInflater.from(context).inflate(R.layout.recycler_view_item, parent, false)
        return ImageHolder(inflatedView)
    }


    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        val loader = ImageLoader.getInstance()


        val options = DisplayImageOptions.Builder().cacheInMemory(true)
            .cacheOnDisc(true).resetViewBeforeLoading(true)
            .showImageForEmptyUri(R.drawable.eclipse_icon)
            .showImageOnFail(R.drawable.eclipse_icon)
            .showImageOnLoading(R.drawable.eclipse_icon).build()

        //download and display image from url
        loader.displayImage(images[position].link, holder.imageView, options)
    }

}

