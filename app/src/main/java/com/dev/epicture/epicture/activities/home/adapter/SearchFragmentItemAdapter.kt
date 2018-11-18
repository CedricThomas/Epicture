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
import com.dev.epicture.epicture.services.imgur.models.ImgurAction
import com.dev.epicture.epicture.services.imgur.models.ImgurType
import com.dev.epicture.epicture.services.imgur.models.PostModel
import com.dev.epicture.epicture.services.imgur.models.SelectableModel
import kotlinx.android.synthetic.main.post_animated_item.view.*
import kotlinx.android.synthetic.main.post_image_item.view.*

/**
 * search fragment adapter
 */
class SearchFragmentItemAdapter(
    imagesFull: ArrayList<PostModel>,
    private val context: Context,
    private val actionActivator: (ImgurAction, PostModel) -> Unit,
    selectActivator: (SelectableAdapter, SelectableModel) -> Boolean)
: SelectableAdapter(selectActivator), Filterable {

    private val imageType = 1
    private val animatedType = 2
    var images = imagesFull

    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    /**
     * return Selected items
     */
    fun getSelection(): ArrayList<PostModel> {
        if (!selecting)
            return ArrayList()
        return ArrayList(images.filter { it ->
            it.selected
        })
    }

    /**
     * astract post holder
     */
    abstract inner class PostHolder(view: View) : SelectableHolder(view) {
        abstract val titleView: TextView
        abstract val viewsView: TextView
        abstract val upView: TextView
        abstract val downView: TextView
        abstract val upImage: ImageView
        abstract val downImage: ImageView
        abstract val favorite: ToggleButton
    }

    /**
     * Image post holder
     */
    inner class ImageHolder(view: View) : PostHolder(view) {
        override val selectToggle: ToggleButton = view.post_image_select_toggle
        override val titleView: TextView = view.post_image_title
        override val viewsView: TextView = view.post_image_view_text
        override val upView: TextView = view.post_image_up_text
        override val downView: TextView = view.post_image_down_text
        val imageView: ImageView = view.post_image_preview
        override val favorite: ToggleButton = view.post_image_favorite_toggle
        override val upImage: ImageView = view.post_image_up
        override val downImage: ImageView = view.post_image_down
    }

    /**
     * Animated post holder
     */
    inner class AnimatedHolder(view: View) : PostHolder(view) {
        override val selectToggle: ToggleButton = view.post_animated_select_toggle
        override val titleView: TextView = view.post_animated_title
        override val viewsView: TextView = view.post_animated_view_text
        override val upView: TextView = view.post_animated_up_text
        override val downView: TextView = view.post_animated_down_text
        val videoView: VideoView = view.post_animated_preview
        val placeholder: ImageView = view.post_animated_placeholder
        override val favorite: ToggleButton = view.post_animated_favorite_toggle
        override val upImage: ImageView = view.post_animated_up
        override val downImage: ImageView = view.post_animated_down
    }

    override fun getItemViewType(position: Int): Int {
        val model = images[position]
        return if (model.mp4Url != null && !model.mp4Url.isEmpty())
            animatedType
        else
            imageType
    }

    /**
     * Create and configure the holder with the good type
     */
    override fun onCreateViewHolder(parent: ViewGroup, type: Int): SelectableHolder {
        return if (type == animatedType) {
            val view = LayoutInflater.from(context).inflate(R.layout.post_animated_item, parent, false)
            AnimatedHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.post_image_item, parent, false)
            ImageHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    /**
     * activate favorite click on holder (based on action callback)
     */
    private fun activateFavorite(holder: PostHolder, position: Int) {
        holder.favorite.setOnClickListener {
            actionActivator(ImgurAction.FAVORITE, images[position])
        }
    }

    /**
     * activate down click on holder (based on action callback)
     */
    private fun activateDown(holder: PostHolder, position: Int) {
        holder.downImage.setOnClickListener {
            if (images[position].vote == "down") {
                actionActivator(ImgurAction.RESET_VOTE, images[position])
                images[position].vote = null
                GlideApp.with(context).load(R.drawable.ic_arrow_drop_down).into(holder.downImage)
                images[position].downNb = images[position].downNb!! - 1
            } else {
                actionActivator(ImgurAction.DOWN, images[position])
                if (images[position].vote == "up")
                    images[position].upNb = images[position].upNb!! - 1
                images[position].vote = "down"
                GlideApp.with(context).load(R.drawable.ic_arrow_drop_down_secondary).into(holder.downImage)
                GlideApp.with(context).load(R.drawable.ic_arrow_up).into(holder.upImage)
                images[position].downNb = images[position].downNb!! + 1
            }
            holder.downView.text = images[position].downNb.toString()
            holder.upView.text = images[position].upNb.toString()
        }
    }

    /**
     * activate up click on holder (based on action callback)
     */
    private fun activateUp(holder: PostHolder, position: Int) {
        holder.upImage.setOnClickListener {
            if (images[position].vote == "up") {
                actionActivator(ImgurAction.RESET_VOTE, images[position])
                images[position].vote = null
                GlideApp.with(context).load(R.drawable.ic_arrow_up).into(holder.upImage)
                images[position].upNb = images[position].upNb!! - 1
            } else {
                actionActivator(ImgurAction.UP, images[position])
                if (images[position].vote == "down")
                    images[position].downNb = images[position].downNb!! - 1
                images[position].vote = "up"
                GlideApp.with(context).load(R.drawable.ic_arrow_up_secondary).into(holder.upImage)
                GlideApp.with(context).load(R.drawable.ic_arrow_drop_down).into(holder.downImage)
                images[position].upNb = images[position].upNb!! + 1
            }
            holder.downView.text = images[position].downNb.toString()
            holder.upView.text = images[position].upNb.toString()
        }
    }

    /**
     * Common bind on post model
     */
    private fun bindPostHolder(holder: PostHolder, position: Int) {

        // Activate title
        if (images[position].title != null && !images[position].title?.isEmpty()!!) {
            holder.titleView.text = images[position].title
            holder.titleView.visibility = View.VISIBLE
        } else {
            holder.titleView.visibility = View.INVISIBLE
        }

        // Activate Stats print
        holder.downView.text = images[position].downNb.toString()
        holder.upView.text = images[position].upNb.toString()
        holder.viewsView.text = images[position].viewNb.toString()
        if (images[position].vote != null && images[position].vote == "up") {
            GlideApp.with(context).load(R.drawable.ic_arrow_up_secondary).into(holder.upImage)
        } else if (images[position].vote != null && images[position].vote == "down") {
            GlideApp.with(context).load(R.drawable.ic_arrow_drop_down_secondary).into(holder.downImage)
        }
        activateUp(holder, position)
        activateDown(holder, position)

        holder.favorite.isChecked = images[position].favorite!!
        activateFavorite(holder, position)
    }

    /**
     * Bind and configure an image holder
     */
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

            //Activate Selection
            activateSelect(holder.imageView, rawHolder, images[position] as SelectableModel) { status ->
                if (status)
                    holder.imageView.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.ADD)
                else
                    holder.imageView.colorFilter = null
            }

            bindPostHolder(holder, position)

        } catch (e: Exception) {
            Log.e("ImageBindError", e.message)
        }
    }

    /**
     * Bind and configure an animated holder
     */
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

            //Activate Selection
            activateSelect(holder.placeholder, rawHolder, images[position] as SelectableModel) { status ->
                if (status) {
                    holder.videoView.setBackgroundColor(Color.argb(150, 255, 255, 255))
                } else {
                    holder.videoView.setBackgroundColor(0)
                }
            }

            bindPostHolder(holder, position)

        } catch (e: Exception) {
            Log.e("VideoBindError", e.message)
        }
    }

    /**
     * auto-select subtype bind
     */
    override fun onBindViewHolder(rawHolder: SelectableHolder, position: Int) {
        if (getItemViewType(position) == animatedType)
            bindAnimatedHolder(rawHolder, position)
        else
            bindImageHolder(rawHolder, position)
    }

    /**
     * empty filter on search adapter (managed by fragment)
     */
    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(charSequence: CharSequence): FilterResults {
                return FilterResults()
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
            }

        }
    }

    /**
     * empty filter on search adapter (no content filter)
     */
    override fun filter(type: ImgurType) {
    }
}