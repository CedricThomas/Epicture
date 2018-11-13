package com.dev.epicture.epicture.home.fragment

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
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
import com.google.gson.JsonElement


class FavoritesFragment : GalleryFragment() {

    private var images: ArrayList<JsonElement> = ArrayList()
    private lateinit var adapter: FavoritesFragmentItemAdapter
    private lateinit var fragView: View


    private fun createRecyclerView() {

        adapter = FavoritesFragmentItemAdapter(images, context!!)
        val recyclerView = fragView.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = adapter
        loadFavoritesPage(0) {
            adapter.notifyDataSetChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragView = inflater.inflate(R.layout.fragment_gallery_favorites, container, false)
        createRecyclerView()
        return fragView
    }

    // load images in
    private fun loadFavoritesPage(page: Int, callback: () -> Unit = {}) {
        ImgurService.getFavorite({ resp ->
            try {
                for (image in resp.data)
                    images.add(image)
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
//    private fun deleteImages(images: ArrayList<ImageModel>) {
//        for (image in images) {
//            ImgurService.deleteImage({
//            }, {
//                MyApplication.printMessage("Failed to delete image ${image.id}")
//            }, image.id!!)
//        }
//    }

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
