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
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.imgur.service.GlideApp
import com.dev.epicture.epicture.imgur.service.models.ImageModel
import kotlinx.android.synthetic.main.recycler_view_item.view.*


class GalleryItemAdapter(private val images : ArrayList<ImageModel>, private val context: Context) : RecyclerView.Adapter<GalleryItemAdapter.ImageHolder>() {

    var selecting = false

    inner class ImageHolder (view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.imageView

        val favButton = view.favorite_toggle!!
        val selButton = view.select_toggle!!

    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ImageHolder {
        val inflatedView = LayoutInflater.from(context).inflate(R.layout.recycler_view_item, parent, false)
        return ImageHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return images.size
    }


    private fun synchroniseSelect(holder: ImageHolder, position: Int) {

        holder.imageView.setOnLongClickListener {
            if (!selecting) {
                images[position].selected = true
                selecting = true
                notifyDataSetChanged()
            }
            return@setOnLongClickListener true
        }

        val toggle = { b: Boolean ->
            images[position].selected = b
            holder.selButton.isChecked = b
            if (b)
                holder.imageView.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.ADD)
            else
                holder.imageView.colorFilter = null
        }

        if (selecting) {

            holder.selButton.visibility = View.VISIBLE

            holder.selButton.setOnCheckedChangeListener(object:View.OnClickListener, CompoundButton.OnCheckedChangeListener {

                override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                    val scaleAnimation = ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f, Animation.RELATIVE_TO_SELF, 0.7f, Animation.RELATIVE_TO_SELF, 0.7f)
                    scaleAnimation.duration = 500
                    val bounceInterpolator = BounceInterpolator()
                    scaleAnimation.interpolator = bounceInterpolator
                    p0?.startAnimation(scaleAnimation)
                    toggle(holder.selButton.isChecked)
                }

                override fun onClick(p0: View?) {
                }

            })
            toggle(images[position].selected)
        } else
            holder.selButton.isChecked = false

    }


    private fun synchroniseFavorite(holder: ImageHolder, position: Int) {
        holder.favButton.setOnCheckedChangeListener(object:View.OnClickListener, CompoundButton.OnCheckedChangeListener {

            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                val scaleAnimation = ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f, Animation.RELATIVE_TO_SELF, 0.7f, Animation.RELATIVE_TO_SELF, 0.7f)
                scaleAnimation.duration = 500
                val bounceInterpolator = BounceInterpolator()
                scaleAnimation.interpolator = bounceInterpolator
                p0?.startAnimation(scaleAnimation)
            }

            override fun onClick(p0: View?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
        holder.favButton.isChecked = images[position].favorite!!
    }

        override fun onBindViewHolder(holder: ImageHolder, position: Int) {

        holder.imageView.setOnClickListener {

        }

        GlideApp
            .with(context)
            .load(images[position].link)
            .placeholder(R.drawable.loader)
            .thumbnail(Glide.with(context).load(R.drawable.loader))
            .dontAnimate()
            .into(holder.imageView)

            synchroniseSelect(holder, position)
            synchroniseFavorite(holder, position)
    }

}

