package com.dev.epicture.epicture.home.Adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.imgur.service.GlideApp
import com.dev.epicture.epicture.imgur.service.models.AlbumModel
import com.dev.epicture.epicture.imgur.service.models.GalleryImageModel
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.post_item.view.*

class FavoritesFragmentItemAdapter(
    imagesFull: ArrayList<JsonElement>,
    private val context: Context
)
: RecyclerView.Adapter<FavoritesFragmentItemAdapter.ImageHolder>(), Filterable {

    var images = imagesFull
    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    // Data Holder /!\ Do not use for status storage
    inner class ImageHolder (view: View) : RecyclerView.ViewHolder(view) {
        val image = view.preview
    }

    // Configure Image Holder
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ImageHolder {
        val inflatedView = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false)
        val holder = ImageHolder(inflatedView)
        return holder
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        if (images[position].asJsonObject.has("images")) {
            val type = object : TypeToken<AlbumModel>() {}.type
            val album = Gson().fromJson<AlbumModel>(images[position], type)
            Log.i("CACACA", album.toString())
            GlideApp
                .with(context)
                .load(album.images?.get(0)?.link)
                .placeholder(R.drawable.loader)
                .thumbnail(Glide.with(context).load(R.drawable.loader))
                .into(DrawableImageViewTarget(holder.image))
        } else {
            val type = object : TypeToken<GalleryImageModel>() {}.type
            val galleryImage = Gson().fromJson<GalleryImageModel>(images[position], type)
            GlideApp
                .with(context)
                .load(galleryImage.link)
                .placeholder(R.drawable.loader)
                .thumbnail(Glide.with(context).load(R.drawable.loader))
                .into(DrawableImageViewTarget(holder.image))
        }

    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(charSequence: CharSequence): FilterResults {
                return FilterResults()
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {

            }

        }

    }

}

