package com.dev.epicture.epicture.home.Adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.imgur.service.models.PostModel
import com.dev.epicture.epicture.imgur.service.models.SelectableModel
import kotlinx.android.synthetic.main.post_image_item.view.*

class FavoritesFragmentItemAdapter(
    imagesFull: ArrayList<PostModel>,
    private val context: Context,
    selectActivator: (SelectableAdapter, SelectableModel) -> Boolean
)
: SelectableAdapter(selectActivator), Filterable {

    var images = imagesFull
    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    // Data Holder /!\ Do not use for status storage
    inner class ImageHolder (view: View) : SelectableAdapter.SelectableHolder(view) {
//        val preview = view.preview!!
        override val selectToggle = view.post_image_favorite_toggle!!
//        val favoriteToggle = view.favorite_toggle!!
//        val title = view.title!!
//        val views = view.view_text!!
//        val up = view.up_text!!
//        val down = view.down_text!!
    }

    // Configure Image Holder
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ImageHolder {
        val inflatedView = LayoutInflater.from(context).inflate(R.layout.post_image_item, parent, false)
        val holder = ImageHolder(inflatedView)
//        holder.favoriteToggle.visibility = View.INVISIBLE
        return holder
    }

    override fun getItemCount(): Int {
        return images.size
    }

    fun getSelection(): ArrayList<PostModel> {
//        if (!selecting)
            return ArrayList()
//        return ArrayList(images.filter { it ->
//            it.selected
//        })
    }

    private fun loadDataInView(holder: ImageHolder, model: PostModel) {
//        holder.up.text = model.upNb.toString()
//        holder.down.text = model.downNb.toString()
//        holder.views.text = model.viewNb.toString()
//        holder.title.text = model.title
    }

    override fun onBindViewHolder(rawHolder: SelectableHolder, position: Int) {

//        val holder : ImageHolder = rawHolder as ImageHolder
//
//        try {
//            // Load image in view
//            GlideApp
//                .with(context)
//                .load(images[position].previewUrl)
//                .placeholder(R.drawable.loader)
//                .thumbnail(Glide.with(context).load(R.drawable.loader))
//                .into(DrawableImageViewTarget(holder.preview))
//
//            // Load date in the view (views, up, down)
//            loadDataInView(holder, images[position])
//
//            //Activate Selection
//            activateSelect(holder.preview, rawHolder, images[position] as SelectableModel) {status ->
//                if (status)
//                    holder.preview.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.ADD)
//                else
//                    holder.preview.colorFilter = null
//            }
//
//        } catch (e: Exception) {
//            Log.e("ImageBindError", e.message)
//        }
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

