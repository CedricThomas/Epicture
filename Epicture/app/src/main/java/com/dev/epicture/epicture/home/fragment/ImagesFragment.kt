package com.dev.epicture.epicture.home.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.home.Adapter.ImagesFragmentItemAdapter
import com.dev.epicture.epicture.home.HomeActivity
import com.dev.epicture.epicture.imgur.service.ImgurService
import com.dev.epicture.epicture.imgur.service.models.ImageModel

class ImagesFragment : Fragment() {

    private var images: ArrayList<ImageModel> = ArrayList()
    private var onNewPage: Boolean = false
    private var page: Int = 0

    private fun activateReload(recyclerView: RecyclerView) {
        // Reload activation
        (activity as HomeActivity).actionMenu?.findItem(R.id.action_refresh)?.isVisible = true
        (activity as HomeActivity).actionMenu?.findItem(R.id.action_refresh)?.setOnMenuItemClickListener {
            images.clear()
            loadActivePages(recyclerView)
            return@setOnMenuItemClickListener true
        }
    }

    // Delete activation
    private fun activateDelete(recyclerView: RecyclerView) {
        (activity as HomeActivity).actionMenu?.findItem(R.id.action_delete)?.setOnMenuItemClickListener {
            val adapter = recyclerView.adapter as ImagesFragmentItemAdapter
            val removed = adapter.applySelection()
            for (elem in removed)
                images.remove(elem)
            deleteImages(removed)
            recyclerView.adapter?.notifyDataSetChanged()
            return@setOnMenuItemClickListener true
        }
    }

    // Cancel activation
    private fun activateCancelSelection(recyclingView: RecyclerView) {
        (activity as HomeActivity).actionMenu?.findItem(R.id.action_cancel)?.setOnMenuItemClickListener {
            val adapter = recyclingView.adapter as ImagesFragmentItemAdapter
            val selected = adapter.applySelection()
            selected.forEach { image ->
                image.selected = false
            }
            recyclingView.adapter?.notifyDataSetChanged()
            return@setOnMenuItemClickListener true
        }
    }

    // Infinite scroll activation
    private fun activateInfiniteScroll(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1) && !onNewPage) {
                    val size = images.size
                    onNewPage = true
                    loadImagesPage(recyclerView, page + 1) {
                        if (images.size > size) {
                            page += 1
                        }
                        onNewPage = false
                    }
                }
            }
        })
    }

    private fun loadActivePages(recyclerView: RecyclerView) {

        for (i in 0..page + 1)
            loadImagesPage(recyclerView, i)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_gallery, container, false)


        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView?.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView?.adapter = ImagesFragmentItemAdapter(images, context!!, activity!!)

        activateReload(recyclerView)
        activateDelete(recyclerView)
        activateCancelSelection(recyclerView)
        activateInfiniteScroll(recyclerView)

        loadActivePages(recyclerView)

        return view
    }


    // add a page in images array and update recycler view
    private fun loadImagesPage(recyclerView: RecyclerView, page: Int, callback: () -> Unit = {}) {
        ImgurService.getImages({ resp ->
            try {
                for (image in resp.data)
                    images.add(image)
                activity!!.runOnUiThread {
                    recyclerView.adapter?.notifyDataSetChanged()
                }
            } catch (e : Exception) {
                Log.i("LoadImages", e.message)
            }
            callback()
        }, {e ->
            Log.i("LoadImages", e.message)
            callback()
        }, page.toString())
    }

    // delete select item from array
    private fun deleteImages(images: ArrayList<ImageModel>) {
        for (image in images) {
            ImgurService.deleteImage({ resp ->
                Log.i("DeleteSelectedImages", resp.asString)
            }, {resp ->
                Log.i("DeleteFailure", resp.message)
            }, image.id!!)
        }

    }

}

