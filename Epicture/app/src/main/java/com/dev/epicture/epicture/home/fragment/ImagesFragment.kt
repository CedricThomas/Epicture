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
import com.dev.epicture.epicture.home.Adapter.GalleryItemAdapter
import com.dev.epicture.epicture.home.HomeActivity
import com.dev.epicture.epicture.imgur.service.ImgurService
import com.dev.epicture.epicture.imgur.service.models.ImageModel

class ImagesFragment : Fragment() {

    private val images: ArrayList<ImageModel> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_gallery, container, false)

        val recyclingView = view.findViewById<RecyclerView>(R.id.recyclingView)
        recyclingView?.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclingView?.adapter = GalleryItemAdapter(images, context!!, activity!!)

        loadImages(recyclingView, 0)

        return view
    }

    private fun loadImages(recyclerView: RecyclerView, page: Int) {
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
        }, {e ->
            Log.i("LoadImages", e.message)
        }, page.toString())
    }

    fun deleteSelectedImages(recyclerView: RecyclerView, images: ArrayList<ImageModel>) {
        val selected = images.filter { it ->
                        it.selected
        }
        var size = selected.size
        val countDown = {
            size -= 1
            if (size == 0) {
                images.clear()
                loadImages(recyclerView, 0)
            }
        }
        for (image in selected) {
            ImgurService.deleteImage({ resp ->
                Log.i("DeleteSelectedImages", resp.asString)
                countDown()
            }, {resp ->
                Log.i("DeleteSelectedImages", resp.message)
                countDown()
            }, image.id!!)
        }

    }

}

