package com.dev.epicture.epicture.home.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation
import android.widget.CompoundButton
import android.widget.ToggleButton
import com.dev.epicture.epicture.imgur.service.models.SelectableModel

abstract class SelectableAdapter(
    private val selectActivator: (SelectableAdapter, SelectableModel) -> Boolean
): RecyclerView.Adapter<SelectableAdapter.SelectableHolder>() {

    var selecting : Boolean = false

    protected fun activateSelect(view: View, holder: SelectableHolder, model: SelectableModel, callback: (Boolean) -> Unit) {

        view.setOnLongClickListener {
            return@setOnLongClickListener selectActivator(this, model)
        }

        // lambda to toogle selection
        val toggle = { status: Boolean ->

            // Set tile selection
            model.selected = status
            holder.selectToggle.isChecked = status

            // Add filter to the tile
            callback(status)
        }

        // Check selection mod and setup animnation / visibility / check
        if (selecting) {
            // selection on
            holder.selectToggle.visibility = View.VISIBLE
            toggle(model.selected)
            holder.selectToggle.setOnCheckedChangeListener(object: View.OnClickListener, CompoundButton.OnCheckedChangeListener {

                override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                    holder.animateToggle(holder.selectToggle)
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

    abstract inner class SelectableHolder(view: View) : RecyclerView.ViewHolder(view) {

        abstract val selectToggle : ToggleButton

        fun animateToggle(button: ToggleButton) {
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
            button.startAnimation(scaleAnimation)
        }

    }
}