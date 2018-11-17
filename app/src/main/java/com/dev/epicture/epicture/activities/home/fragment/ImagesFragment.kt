package com.dev.epicture.epicture.activities.home.fragment

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.dev.epicture.epicture.MyApplication
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.activities.home.adapter.GalleryFragmentItemAdapter
import com.dev.epicture.epicture.services.imgur.ImgurService
import com.dev.epicture.epicture.services.imgur.models.ImageModel
import com.dev.epicture.epicture.services.imgur.models.ImgurType


class ImagesFragment : GalleryFragment() {

    private lateinit var fragView: View
    private lateinit var adapter: GalleryFragmentItemAdapter
    private var images: ArrayList<ImageModel> = ArrayList()
    private var loading: Boolean = false
    private var page: Int = 0
    private var selectAllStatus = true
    private lateinit var recyclerView: RecyclerView

    // activate / deactivate selection in ActionBar
    private fun setSelectionMode(status: Boolean)  {
        menuManager.delete.isVisible = status
        menuManager.cancel.isVisible = status
        menuManager.refresh.isVisible = !status
        menuManager.spinnerItem.isVisible = !status
    }

    // Filter Activation
    private fun activateFilter() {

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
                    else -> adapter.filter(ImgurType.ALL)
                }
            }
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
            setSelectionMode(false)
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
            setSelectionMode(false)
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

    // load all active pages in images
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

    // configure RecyclerView and active actionBar actions
    private fun createRecyclerView() {

        adapter = GalleryFragmentItemAdapter(images, context!!) { adapter, model ->
            if (!adapter.selecting) {
                model.selected = true
                adapter.selecting = true
                setSelectionMode(true)
                adapter.notifyDataSetChanged()
            }
            true
        }
        recyclerView = fragView.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = adapter
        loadActivePages(recyclerView)
        activateReload(recyclerView)
        activateDelete()
        activateCancelSelection()
        activateInfiniteScroll(recyclerView)
        activateFilter()
    }

    // configure recycler view and inflate view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragView = inflater.inflate(R.layout.fragment_gallery_images, container, false)
        createRecyclerView()
        return fragView
    }

    // add a page in images array and call callback at end
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

    // delete select item from array on imgur
    private fun deleteImages(images: ArrayList<ImageModel>) {
        for (image in images) {
            ImgurService.deleteImage({
            }, {
                MyApplication.printMessage("Failed to delete image ${image.id}")
            }, image.id!!)
        }

    }

    // add a search listener mapped on adapter filter
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

