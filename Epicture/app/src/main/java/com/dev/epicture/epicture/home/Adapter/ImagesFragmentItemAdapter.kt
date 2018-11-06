package com.dev.epicture.epicture.home.Adapter

import android.app.Activity
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
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.home.HomeActivity
import com.dev.epicture.epicture.imgur.service.GlideApp
import com.dev.epicture.epicture.imgur.service.models.ImageModel
import kotlinx.android.synthetic.main.image_item.view.*
import java.lang.Exception


class ImagesFragmentItemAdapter(private var images : ArrayList<ImageModel>, private val context: Context, private val activity: Activity)
: RecyclerView.Adapter<ImagesFragmentItemAdapter.ImageHolder>() {

    private var selecting = false
    private var mRecyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mRecyclerView = recyclerView
    }

    // Data Holder /!\ Do not use for status storage
    inner class ImageHolder (view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.imageView
        val favButton = view.favorite_toggle!!
        val selButton = view.select_toggle!!
    }

    fun applySelection(): ArrayList<ImageModel> {
        if (!selecting)
            return ArrayList()
        selecting = false
        setActionsVisibility(false)
        return ArrayList(images.filter { it ->
            it.selected
        })
    }

    // activate / deactivate ActionBar
    private fun setActionsVisibility(status: Boolean) {
        (activity as HomeActivity).actionMenu?.findItem(R.id.action_delete)?.isVisible = status
        activity.actionMenu?.findItem(R.id.action_cancel)?.isVisible = status
    }

    // Configure Image Holder
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ImageHolder {
        val inflatedView = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false)
        val holder = ImageHolder(inflatedView)
        holder.favButton.visibility = View.INVISIBLE
        return holder
    }

    override fun getItemCount(): Int {
        return images.size
    }

    private fun synchroniseSelect(holder: ImageHolder, model: ImageModel) {

        // Activation of select mod
        holder.imageView.setOnLongClickListener {
            if (!selecting) {
                model.selected = true
                selecting = true
                setActionsVisibility(true)
                notifyDataSetChanged()
            }
            return@setOnLongClickListener true
        }

        // lambda to toogle selection
        val toggle = { b: Boolean ->
            model.selected = b
            holder.selButton.isChecked = b
            if (b)
                holder.imageView.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.ADD)
            else
                holder.imageView.colorFilter = null
        }

        // Check selection mod and setup animnation / visibility / check
        if (selecting) {

            holder.selButton.visibility = View.VISIBLE
            holder.selButton.setOnCheckedChangeListener(object:View.OnClickListener, CompoundButton.OnCheckedChangeListener {

                override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
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
                    p0?.startAnimation(scaleAnimation)
                    toggle(holder.selButton.isChecked)
                }

                override fun onClick(p0: View?) {}

            })
            toggle(model.selected)

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
                .dontAnimate()
                .into(holder.imageView)

            // Setup view for selection
            synchroniseSelect(holder, images[position])
        } catch (e: Exception) {
            Log.e("ImageBindError", e.message)
        }
    }

}
