package com.dev.epicture.epicture.home.Adapter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation
import android.widget.CompoundButton
import android.widget.Filter
import android.widget.Filterable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.imgur.service.GlideApp
import com.dev.epicture.epicture.imgur.service.models.ImageModel
import com.dev.epicture.epicture.imgur.service.models.PostModel
import kotlinx.android.synthetic.main.post_item.view.*

class FavoritesFragmentItemAdapter(
    imagesFull: ArrayList<PostModel>,
    private val context: Context,
    private val selectActivator: (FavoritesFragmentItemAdapter, PostModel) -> Boolean
)
: RecyclerView.Adapter<FavoritesFragmentItemAdapter.ImageHolder>(), Filterable {

    var images = imagesFull
    var selecting = false
    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    // Data Holder /!\ Do not use for status storage
    inner class ImageHolder (view: View) : RecyclerView.ViewHolder(view) {
        val preview = view.preview!!
        val selectToggle = view.select_toggle!!
        val favoriteToggle = view.favorite_toggle!!
        val title = view.title!!
        val views = view.view_text!!
        val up = view.up_text!!
        val down = view.down_text!!
    }

    // Configure Image Holder
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ImageHolder {
        val inflatedView = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false)
        val holder = ImageHolder(inflatedView)
        holder.favoriteToggle.visibility = View.INVISIBLE
        return holder
    }

    override fun getItemCount(): Int {
        return images.size
    }


    fun getSelection(): ArrayList<PostModel> {
        if (!selecting)
            return ArrayList()
        return ArrayList(images.filter { it ->
            it.selected
        })
    }

    private fun activateSelect(holder: FavoritesFragmentItemAdapter.ImageHolder, model: PostModel) {

        // Activation of select mod
        holder.preview.setOnLongClickListener {
            return@setOnLongClickListener selectActivator(this, model)
        }

    }

    private fun animateButton(button: CompoundButton?) {
        // animation on select
        val scaleAnimation = ScaleAnimation(
            0.7f,
            1.0f,
            0.7f,
            1.0f,
            Animation.RELATIVE_TO_SELF,
            0.7f,
            Animation.RELATIVE_TO_SELF,
            0.7f
        )
        scaleAnimation.duration = 500
        val bounceInterpolator = BounceInterpolator()
        scaleAnimation.interpolator = bounceInterpolator
        button?.startAnimation(scaleAnimation)
    }

    private fun synchroniseSelect(holder: FavoritesFragmentItemAdapter.ImageHolder, model: PostModel) {

        // lambda to toogle selection
        val toggle = { status: Boolean ->

            // Set tile selection
            model.selected = status
            holder.selectToggle.isChecked = status

            // Add filter to the tile
            if (status)
                holder.preview.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.ADD)
            else
                holder.preview.colorFilter = null
        }

        // Check selection mod and setup animnation / visibility / check
        if (selecting) {
            // selection on
            holder.selectToggle.visibility = View.VISIBLE
            toggle(model.selected)
            holder.selectToggle.setOnCheckedChangeListener(object:View.OnClickListener, CompoundButton.OnCheckedChangeListener {

                override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                    animateButton(p0)
                    toggle(holder.selectToggle.isChecked)
                }

                override fun onClick(p0: View?) {}

            })

        } else {
            // Set default selection
            toggle(false)
            holder.selectToggle.visibility = View.INVISIBLE
        }

    }

    private fun loadDataInView(holder: ImageHolder, model: PostModel) {
        holder.up.text = model.upNb.toString()
        holder.down.text = model.downNb.toString()
        holder.views.text = model.viewNb.toString()
        holder.title.text = model.title
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {

        try {
            // Load image in view
            GlideApp
                .with(context)
                .load(images[position].previewUrl)
                .placeholder(R.drawable.loader)
                .thumbnail(Glide.with(context).load(R.drawable.loader))
                .into(DrawableImageViewTarget(holder.preview))

            // Load date in the view (views, up, down)
            loadDataInView(holder, images[position])
            //Activate Selection
            activateSelect(holder, images[position])
            // Setup view for selection
            synchroniseSelect(holder, images[position])

        } catch (e: Exception) {
            Log.e("ImageBindError", e.message)
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

