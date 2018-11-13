package com.dev.epicture.epicture.home.Adapter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
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
import com.dev.epicture.epicture.imgur.service.models.ImageModel
import com.dev.epicture.epicture.imgur.service.models.SelectableModel
import kotlinx.android.synthetic.main.image_item.view.*


class ImagesFragmentItemAdapter(
    private var imagesFull: ArrayList<ImageModel>,
    private val context: Context,
    selectActivator: (SelectableAdapter, SelectableModel) -> Boolean)
: SelectableAdapter(selectActivator), Filterable {

    var images = imagesFull
    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    // Data Holder /!\ Do not use for status storage
    inner class ImageHolder (view: View) : SelectableHolder(view) {
        override val selectToggle: ToggleButton = view.select_toggle
        val imageView: ImageView = view.preview
        val textView: TextView = view.title
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

    private fun activateTitle(holder: ImageHolder, position: Int) {
        // Activation of title
        if (images[position].title != null && !images[position].title?.isEmpty()!!) {
            holder.textView.text = images[position].title
            holder.textView.visibility = View.VISIBLE
        } else {
            holder.textView.visibility = View.INVISIBLE
        }
    }
        
    override fun onBindViewHolder(rawHolder: SelectableHolder, position: Int) {

        val holder : ImageHolder = rawHolder as ImageHolder
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

