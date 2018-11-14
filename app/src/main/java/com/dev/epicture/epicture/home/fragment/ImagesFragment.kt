package com.dev.epicture.epicture.home.fragment

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dev.epicture.epicture.MyApplication
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.home.adapter.GalleryFragmentItemAdapter
import com.dev.epicture.epicture.imgur.service.ImgurService
import com.dev.epicture.epicture.imgur.service.models.ImageModel


class ImagesFragment : GalleryFragment() {

    private lateinit var fragView: View
    private lateinit var adapter: GalleryFragmentItemAdapter
    private var images: ArrayList<ImageModel> = ArrayList()
    private var loading: Boolean = false
    private var page: Int = 0
    private var selectAllStatus = true

    // activate / deactivate ActionBar
    private fun setActionsVisibility(status: Boolean)  {
        menuManager.delete.isVisible = status
        menuManager.cancel.isVisible = status
        menuManager.refresh.isVisible = !status
    }

    // Select selectAllStatus Activation
    private fun activateSelectAll(recyclerView: RecyclerView) {
        // Reload activation
        menuManager.selectAll.isVisible = true
        menuManager.selectAll.setOnMenuItemClickListener {
            val adapter = recyclerView.adapter as GalleryFragmentItemAdapter
            adapter.selecting = true
            for (elem in images)
                elem.selected = selectAllStatus
            selectAllStatus = !selectAllStatus
            setActionsVisibility(true)
            adapter.notifyDataSetChanged()
            return@setOnMenuItemClickListener true
        }
    }

    // Reload Activation
    private fun activateReload(recyclerView: RecyclerView) {
        // Reload activation
        menuManager.refresh.isVisible = true
        menuManager.refresh.setOnMenuItemClickListener {
            selectAllStatus = false
            loadActivePages(recyclerView)
            return@setOnMenuItemClickListener true
        }
    }

    // Delete activation
    private fun activateDelete() {
        menuManager.delete.setOnMenuItemClickListener {
            selectAllStatus = false
            val removed = adapter.getSelection()
            for (elem in removed)
                images.remove(elem)
            deleteImages(removed)
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
                    loadImagesPage(page + 1) {
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

    private fun createRecyclerView() {

        adapter = GalleryFragmentItemAdapter(images, context!!) { adapter, model ->
            if (!adapter.selecting) {
                model.selected = true
                adapter.selecting = true
                setActionsVisibility(true)
                adapter.notifyDataSetChanged()
            }
            true
        }
        val recyclerView = fragView.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = adapter

        loadActivePages(recyclerView)
        activateReload(recyclerView)
        activateDelete()
        activateCancelSelection()
        activateInfiniteScroll(recyclerView)
        activateSelectAll(recyclerView)

    }

    private fun loadActivePages(recyclerView: RecyclerView) {
        if (loading)
            return
        images.clear()
        loading = true
        for (i in 0..page)
            loadImagesPage(i) {
                if (i == page) {
                    activity?.runOnUiThread {
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                    loading = false
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragView = inflater.inflate(R.layout.fragment_gallery_images, container, false)
        createRecyclerView()
        return fragView
    }

    // add a page in images array and update recycler view
    private fun loadImagesPage(page: Int, callback: () -> Unit = {}) {
        ImgurService.getImages({ resp ->
            try {
                for (image in resp.data)
                    images.add(image)
                images.distinctBy { it.id }
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
    private fun deleteImages(images: ArrayList<ImageModel>) {
        for (image in images) {
            ImgurService.deleteImage({
            }, {
                MyApplication.printMessage("Failed to delete image ${image.id}")
            }, image.id!!)
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

