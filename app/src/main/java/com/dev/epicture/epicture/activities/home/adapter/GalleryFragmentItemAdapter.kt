package com.dev.epicture.epicture.activities.home.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.services.glide.GlideApp
import com.dev.epicture.epicture.services.imgur.models.ImageModel
import com.dev.epicture.epicture.services.imgur.models.ImgurType
import com.dev.epicture.epicture.services.imgur.models.SelectableModel
import kotlinx.android.synthetic.main.gallery_animated_item.view.*
import kotlinx.android.synthetic.main.gallery_image_item.view.*


class GalleryFragmentItemAdapter(
    private var imagesFull: ArrayList<ImageModel>,
    private val context: Context,
    selectActivator: (SelectableAdapter, SelectableModel) -> Boolean)
: SelectableAdapter(selectActivator), Filterable {

    private val imageType = 1
    private val animatedType = 2
    var images = imagesFull
    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    fun getSelection(): ArrayList<ImageModel> {
        if (!selecting)
            return ArrayList()
        return ArrayList(images.filter { it ->
            it.selected
        })
    }

    inner class ImageHolder (view: View) : SelectableHolder(view) {
        override val selectToggle: ToggleButton = view.gallery_image_select_toggle
        val imageView: ImageView = view.gallery_image_preview
        val textView: TextView = view.gallery_image_title
    }

    inner class AnimatedHolder(view: View) : SelectableHolder(view) {
        override val selectToggle: ToggleButton = view.gallery_animated_select_toggle
        val videoView: VideoView = view.gallery_animated_preview
        val placeholder: ImageView = view.gallery_animated_filter
        val textView: TextView = view.gallery_animated_title
    }

    override fun getItemViewType(position: Int): Int {
        val model = images[position]
        return if (model.mp4 != null && !model.mp4.isEmpty())
            animatedType
        else
            imageType
    }

    // Configure Image Holder
    override fun onCreateViewHolder(parent: ViewGroup, type: Int): SelectableHolder {
        return if (type == animatedType)
            AnimatedHolder(LayoutInflater.from(context).inflate(R.layout.gallery_animated_item, parent, false))
        else
            ImageHolder(LayoutInflater.from(context).inflate(R.layout.gallery_image_item, parent, false))
    }

    override fun getItemCount(): Int {
        return images.size
    }

    private fun bindImageHolder(rawHolder: SelectableHolder, position: Int) {
        try {
            val holder : ImageHolder = rawHolder as ImageHolder
            // Load image in view
            val imageViewTarget = DrawableImageViewTarget(holder.imageView)

            GlideApp
                .with(context)
                .load(images[position].link)
                .placeholder(R.drawable.loader)
                .thumbnail(Glide.with(context).load(R.drawable.loader))
                .into(imageViewTarget)

            // Activate title
            if (images[position].title != null && !images[position].title?.isEmpty()!!) {
                holder.textView.text = images[position].title
                holder.textView.visibility = View.VISIBLE
            } else {
                holder.textView.visibility = View.INVISIBLE
            }

            //Activate Selection
            activateSelect(holder.imageView, rawHolder, images[position] as SelectableModel) { status ->
                if (status)
                    holder.imageView.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.ADD)
                else
                    holder.imageView.colorFilter = null
            }

        } catch (e: Exception) {
            Log.e("ImageBindError", e.message)
        }
    }

    private fun bindAnimatedHolder(rawHolder: SelectableHolder, position: Int) {
        try {
            val holder : AnimatedHolder = rawHolder as AnimatedHolder
            // Load video in view
            val imageViewTarget = DrawableImageViewTarget(holder.placeholder)
            holder.placeholder.alpha = 1F
            GlideApp.with(context).load(R.drawable.loader).into(imageViewTarget)
            holder.videoView.setVideoURI(Uri.parse(images[position].mp4))
            holder.videoView.setOnPreparedListener { mp ->
                holder.placeholder.setBackgroundColor(Color.WHITE)
                holder.placeholder.alpha = 0F
                mp.setVolume(0F, 0F)
                mp.isLooping = true
            }
            holder.videoView.start()

            // Activate title
            if (images[position].title != null && !images[position].title?.isEmpty()!!) {
                holder.textView.text = images[position].title
                holder.textView.visibility = View.VISIBLE
            } else {
                holder.textView.visibility = View.INVISIBLE
            }

            //Activate Selection
            activateSelect(holder.placeholder, rawHolder, images[position] as SelectableModel) { status ->
                if (status)
                    holder.videoView.setBackgroundColor(Color.argb(150, 255, 255, 255))
                else
                    holder.videoView.setBackgroundColor(0)
            }

        } catch (e: Exception) {
            Log.e("VideoBindError", e.message)
        }
    }

    override fun onBindViewHolder(rawHolder: SelectableHolder, position: Int) {
        if (getItemViewType(position) == animatedType)
            bindAnimatedHolder(rawHolder, position)
        else
            bindImageHolder(rawHolder, position)
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                images = if (charString.isEmpty()) {
                    imagesFull
                } else {
                    val base = ArrayList(imagesFull.filter { it.title != null })
                    val filteredList = ArrayList<ImageModel>()
                    for (row in base)
                        if (row.description?.contains(charString)!! || row.title?.contains(charString)!!)
                            filteredList.add(row)
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = images
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                try {
                    images = (filterResults.values  as? ArrayList<ImageModel>)!!
                    notifyDataSetChanged()
                } catch (e: Exception) {

                }
            }
        }
    }

    override fun filter(type: ImgurType) {
        val filter: (ImageModel) -> Boolean  = when (type) {
            ImgurType.ALL -> {{
                true
            }}
            ImgurType.IMAGES -> {{it ->
                it.mp4 == null
            }}
            ImgurType.GIFS -> {{
                it.mp4 != null
            }}
        }
        val new: ArrayList<ImageModel> = ArrayList()
        for (elem in imagesFull)
            if (filter(elem))
                new.add(elem)
        images = new
        notifyDataSetChanged()
    }

}

