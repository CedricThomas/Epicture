package com.dev.epicture.epicture.activities.home.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.dev.epicture.epicture.MyApplication
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.activities.home.adapter.FavoritesFragmentItemAdapter
import com.dev.epicture.epicture.activities.home.decorators.ItemOffsetDecoration
import com.dev.epicture.epicture.services.imgur.ImgurService
import com.dev.epicture.epicture.services.imgur.models.*
import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlinx.android.synthetic.main.fragment_gallery_favorites.*


class FavoritesFragment : GalleryFragment() {

    private var images: ArrayList<PostModel> = ArrayList()
    private lateinit var adapter: FavoritesFragmentItemAdapter
    private lateinit var fragView: View
    private var loading: Boolean = false
    private var page: Int = 0

    // activate / deactivate ActionBar
    private fun setActionsVisibility(status: Boolean)  {
        menuManager.delete.isVisible = status
        menuManager.cancel.isVisible = status
        menuManager.refresh.isVisible = !status
        menuManager.spinnerItem.isVisible = !status
    }

    // Filter Activation
    private fun activateFilter() {
        menuManager.spinner.visibility = View.VISIBLE
        menuManager.spinnerItem.isVisible = true
        menuManager.spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                if (images.size == 0)
                    return
                adapter.filter(ImgurType.ALL)
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (images.size == 0)
                    return
                when(position) {
                    0 -> adapter.filter(ImgurType.ALL)
                    1 -> adapter.filter(ImgurType.GIFS)
                    2 -> adapter.filter(ImgurType.IMAGES)
                }
            }
        }
    }

    // Reload Activation
    private fun activateReload(recyclerView: RecyclerView) {
        // Reload activation
        menuManager.refresh.isVisible = true
        menuManager.refresh.setOnMenuItemClickListener {
            loadActivePages(recyclerView)
            return@setOnMenuItemClickListener true
        }
    }

    // Delete activation
    private fun activateDelete() {
        menuManager.delete.setOnMenuItemClickListener {
            val removed = adapter.getSelection()
            for (elem in removed)
                images.remove(elem)
            updateEmptyImage()
            deleteFavorites(removed)
            adapter.selecting = false
            setActionsVisibility(false)
            adapter.notifyDataSetChanged()
            return@setOnMenuItemClickListener true
        }
    }

    // Cancel activation
    private fun activateCancelSelection() {
        menuManager.cancel.setOnMenuItemClickListener {
            val selected = adapter.getSelection()
            selected.forEach { image ->
                image.selected = false
            }
            adapter.selecting = false
            setActionsVisibility(false)
            adapter.notifyDataSetChanged()
            return@setOnMenuItemClickListener true
        }
    }

    // Infinite scroll activation
    private fun activateInfiniteScroll(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1) && !loading) {
                    val size = images.size
                    loading = true
                    loadFavoritePage(page + 1) {
                        if (images.size > size) {
                            page += 1
                        }
                        loading = false
                        activity?.runOnUiThread {
                            recyclerView.adapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
        })
    }

    private fun updateEmptyImage() {
        if (images.isEmpty()) {
            recycler_view.visibility = View.GONE
            empty.visibility = View.VISIBLE
        } else {
            recycler_view.visibility = View.VISIBLE
            empty.visibility = View.GONE
        }

    }

    private fun loadActivePages(recyclerView: RecyclerView) {
        if (loading)
            return
        images.clear()
        loading = true
        for (i in 0..page)
            loadFavoritePage(i) {
                if (i == page) {
                    activity?.runOnUiThread {
                        updateEmptyImage()
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                    loading = false
                }
            }
    }

    private fun createRecyclerView() {

        adapter = FavoritesFragmentItemAdapter(images, context!!) { adapter, model ->
            if (!adapter.selecting) {
                model.selected = true
                adapter.selecting = true
                setActionsVisibility(true)
                adapter.notifyDataSetChanged()
            }
            true
        }
        val recyclerView = fragView.findViewById<RecyclerView>(R.id.recycler_view)
        val itemDecoration = ItemOffsetDecoration(5)
        recyclerView.addItemDecoration(itemDecoration)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        loadActivePages(recyclerView)
        activateCancelSelection()
        activateDelete()
        activateInfiniteScroll(recyclerView)
        activateReload(recyclerView)
        activateFilter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragView = inflater.inflate(R.layout.fragment_gallery_favorites, container, false)
        createRecyclerView()
        return fragView
    }

    private fun elementToPost(element: JsonElement): PostModel {
        val gson = Gson()
        if (element.asJsonObject.has("images")) {
            val data : AlbumModel = gson.fromJson(element, AlbumModel::class.java)
            // Album
            return PostModel(
                data.id,
                data.title,
                data.views,
                data.ups,
                data.downs,
                data.images?.get(0)?.link,
                data.images?.get(0)?.mp4,
                element,
                PostType.Album,
                data.favorite,
                data.vote,
                element.asJsonObject.get("is_album").asBoolean
            )
        } else {
            val data : GalleryImageModel = gson.fromJson(element, GalleryImageModel::class.java)
            // Gallery Image
            return PostModel(
                data.id,
                data.title,
                data.views,
                data.ups,
                data.downs,
                data.link,
                data.mp4,
                element,
                PostType.GalleryImage,
                data.favorite,
                data.vote,
                element.asJsonObject.get("is_album").asBoolean
            )
        }
    }

    // load favorites
    private fun loadFavoritePage(page: Int, callback: () -> Unit = {}) {
        ImgurService.getFavorite({ resp ->
            try {
                for (image in resp.data) {
                    images.add(elementToPost(image))
                }
            } catch (e : Exception) {
                MyApplication.printMessage("Failed to load images page $page")
            }
            callback()
        }, {
            MyApplication.printMessage("Failed to load images page $page")
            callback()
        }, page.toString())
    }

    // delete select item from array
    private fun deleteFavorites(images: ArrayList<PostModel>) {
        for (image in images) {
            if (image.is_album)
                ImgurService.favoriteAlbum({}, {}, image.id!!)
            else
                ImgurService.favoriteImage({}, {}, image.id!!)
        }
    }

    // add a search listener
    override fun getSearchListener(): SearchView.OnQueryTextListener {
        return object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                adapter.filter.filter(query)
                return false
            }

        }
    }

}
