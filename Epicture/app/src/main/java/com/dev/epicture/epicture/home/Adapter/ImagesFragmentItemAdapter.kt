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
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.imgur.service.GlideApp
import com.dev.epicture.epicture.imgur.service.models.ImageModel
import kotlinx.android.synthetic.main.image_item.view.*


class ImagesFragmentItemAdapter(
    private var imagesFull: ArrayList<ImageModel>,
    private val context: Context,
    private val selectActivator: (ImagesFragmentItemAdapter, ImageModel) -> Boolean)
: RecyclerView.Adapter<ImagesFragmentItemAdapter.ImageHolder>(), Filterable {

    var images = imagesFull
    var selecting = false
    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    // Data Holder /!\ Do not use for status storage
    inner class ImageHolder (view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.preview
        val textView: TextView = view.title
        val selButton = view.select_toggle!!
    }

    fun getSelection(): ArrayList<ImageModel> {
        if (!selecting)
            return ArrayList()
        return ArrayList(images.filter { it ->
            it.selected
        })
    }

    // Configure Image Holder
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ImageHolder {
        val inflatedView = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false)
        return ImageHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    private fun activateSelect(holder: ImageHolder, model: ImageModel) {

        // Activation of select mod
        holder.imageView.setOnLongClickListener {
            return@setOnLongClickListener selectActivator(this, model)
        }

    }

    private fun activateTitle(holder: ImageHolder, position: Int) {
        // Activation of title
        if (images[position].title != null && !images[position].title?.isEmpty()!!) {
            holder.textView.text = images[position].title
            holder.textView.visibility = View.VISIBLE
        } else {
            holder.textView.visibility = View.INVISIBLE
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

    private fun synchroniseSelect(holder: ImageHolder, model: ImageModel) {

        // lambda to toogle selection
        val toggle = { status: Boolean ->

            // Set tile selection
            model.selected = status
            holder.selButton.isChecked = status

            // Add filter to the tile
            if (status)
                holder.imageView.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.ADD)
            else
                holder.imageView.colorFilter = null
        }

        // Check selection mod and setup animnation / visibility / check
        if (selecting) {
            // selection on
            holder.selButton.visibility = View.VISIBLE
            toggle(model.selected)
            holder.selButton.setOnCheckedChangeListener(object:View.OnClickListener, CompoundButton.OnCheckedChangeListener {

                override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                    animateButton(p0)
                    toggle(holder.selButton.isChecked)
                }

                override fun onClick(p0: View?) {}

            })

        } else {
            // Set default selection
            toggle(false)
            holder.selButton.visibility = View.INVISIBLE
        }

    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {

        try {
            // Load image in view
            GlideApp
                .with(context)
                .load(images[position].link)
                .placeholder(R.drawable.loader)
                .thumbnail(Glide.with(context).load(R.drawable.loader))
                .into(DrawableImageViewTarget(holder.imageView))

            // Activate title
            activateTitle(holder, position)
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
                val charString = charSequence.toString()
                images = if (charString.isEmpty()) {
                    imagesFull
                } else {
                    val filteredList = ArrayList<ImageModel>()
                    for (row in imagesFull)
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

}

