package com.dev.epicture.epicture

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class GalleryFragment : Fragment() {

    val images: ArrayList<String> = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_gallery, container, false)

        val rv_animal_list = rootView.findViewById<RecyclerView>(R.id.recyclingView)
        rv_animal_list?.layoutManager = GridLayoutManager(activity, 2)
        rv_animal_list?.adapter = GalleryItemAdapter(images, context!!)
        return rootView
    }


}

