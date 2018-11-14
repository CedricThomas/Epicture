package com.dev.epicture.epicture.home.adapter

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
import com.dev.epicture.epicture.imgur.service.GlideApp
import com.dev.epicture.epicture.imgur.service.models.PostModel
import com.dev.epicture.epicture.imgur.service.models.SelectableModel
import kotlinx.android.synthetic.main.gallery_animated_item.view.*
import kotlinx.android.synthetic.main.gallery_image_item.view.*
import kotlinx.android.synthetic.main.post_animated_item.view.*
import kotlinx.android.synthetic.main.post_image_item.view.*

class FavoritesFragmentItemAdapter(
    private val imagesFull: ArrayList<PostModel>,
    private val context: Context,
    selectActivator: (SelectableAdapter, SelectableModel) -> Boolean
)
: SelectableAdapter(selectActivator), Filterable {

    private val imageType = 1
    private val animatedType = 2
    var images = imagesFull
    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    fun getSelection(): ArrayList<PostModel> {
        if (!selecting)
            return ArrayList()
        return ArrayList(images.filter { it ->
            it.selected
        })
    }

    inner class ImageHolder (view: View) : SelectableHolder(view) {
        override val selectToggle: ToggleButton = view.post_image_select_toggle
        val imageView: ImageView = view.post_image_preview
        val titleView: TextView = view.post_image_title
        val viewsView: TextView = view.post_image_view_text
        val upView: TextView = view.post_image_up_text
        val downView: TextView = view.post_image_down_text
    }

    inner class AnimatedHolder(view: View) : SelectableHolder(view) {
        override val selectToggle: ToggleButton = view.post_animated_select_toggle
        val videoView: VideoView = view.post_animated_preview
        val placeholder: ImageView = view.post_animated_placeholder
        val titleView: TextView = view.post_animated_title
    }

    override fun getItemViewType(position: Int): Int {
        val model = images[position]
        return if (model.mp4Url != null && !model.mp4Url.isEmpty())
            animatedType
        else
            imageType
    }

    // Configure Image Holder
    override fun onCreateViewHolder(parent: ViewGroup, type: Int): SelectableHolder {
        return if (type == animatedType)
            AnimatedHolder(LayoutInflater.from(context).inflate(R.layout.post_animated_item, parent, false))
        else
            ImageHolder(LayoutInflater.from(context).inflate(R.layout.post_image_item, parent, false))
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
                .load(images[position].previewUrl)
                .placeholder(R.drawable.loader)
                .thumbnail(Glide.with(context).load(R.drawable.loader))
                .into(imageViewTarget)

            // Activate title
            if (images[position].title != null && !images[position].title?.isEmpty()!!) {
                holder.titleView.text = images[position].title
                holder.titleView.visibility = View.VISIBLE
            } else {
                holder.titleView.visibility = View.INVISIBLE
            }

            holder.downView.text = images[position].downNb.toString()
            holder.upView.text = images[position].upNb.toString()
            holder.viewsView.text = images[position].viewNb.toString()


            //Activate Selection
            activateSelect(holder.imageView, rawHolder, images[position] as SelectableModel) {status ->
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
            holder.videoView.setVideoURI(Uri.parse(images[position].mp4Url))
            holder.videoView.setOnPreparedListener { mp ->
                holder.placeholder.setBackgroundColor(Color.WHITE)
                holder.placeholder.alpha = 0F
                mp.setVolume(0F, 0F)
                mp.isLooping = true
            }
            holder.videoView.start()

            // Activate title
            if (images[position].title != null && !images[position].title?.isEmpty()!!) {
                holder.titleView.text = images[position].title
                holder.titleView.visibility = View.VISIBLE
            } else {
                holder.titleView.visibility = View.INVISIBLE
            }

            //Activate Selection
            activateSelect(holder.placeholder, rawHolder, images[position] as SelectableModel) {status ->
                if (status) {
                    holder.videoView.setBackgroundColor(Color.argb(150, 255, 255, 255))
                } else {
                    holder.videoView.setBackgroundColor(0)
                }
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
                    val filteredList = ArrayList<PostModel>()
                    for (row in imagesFull)
                        if (row.title?.contains(charString)!!)
                            filteredList.add(row)
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = images
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                try {
                    images = (filterResults.values  as? ArrayList<PostModel>)!!
                    notifyDataSetChanged()
                } catch (e: Exception) {

                }
            }
        }
    }

}

