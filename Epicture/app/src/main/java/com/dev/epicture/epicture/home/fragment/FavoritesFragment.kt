package com.dev.epicture.epicture.home.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dev.epicture.epicture.MyApplication
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.home.Adapter.FavoritesFragmentItemAdapter
import com.dev.epicture.epicture.imgur.service.ImgurService
import com.dev.epicture.epicture.imgur.service.models.AlbumModel
import com.dev.epicture.epicture.imgur.service.models.GalleryImageModel
import com.dev.epicture.epicture.imgur.service.models.PostModel
import com.dev.epicture.epicture.imgur.service.models.PostType
import com.google.gson.Gson
import com.google.gson.JsonElement


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

    private fun loadActivePages(recyclerView: RecyclerView) {
        if (loading)
            return
        images.clear()
        loading = true
        for (i in 0..page)
            loadFavoritePage(i) {
                if (i == page) {
                    activity?.runOnUiThread {
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
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        loadActivePages(recyclerView)
        activateCancelSelection()
        activateDelete()
        activateInfiniteScroll(recyclerView)
        activateReload(recyclerView)
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
                data.title,
                0,
                0,
                0,
                data.images?.get(0)?.link,
                element,
                PostType.Album
            )
        } else {
            val data : GalleryImageModel = gson.fromJson(element, GalleryImageModel::class.java)
            // Gallery Image
            return PostModel(
                data.title,
                0,
                0,
                0,
                data.link,
                element,
                PostType.Album
            )
        }
    }

    // load favorites
    private fun loadFavoritePage(page: Int, callback: () -> Unit = {}) {
        ImgurService.getFavorite({ resp ->
            try {
                for (image in resp.data)
                    images.add(elementToPost(image))
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
