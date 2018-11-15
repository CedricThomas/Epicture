package com.dev.epicture.epicture.activities.home.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.SearchView
import com.dev.epicture.epicture.activities.home.HomeActivity

import com.dev.epicture.epicture.activities.home.HomeActivity.ActionMenuManager

open class GalleryFragment : Fragment() {

    lateinit var menuManager: ActionMenuManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = (activity as HomeActivity)
        menuManager = activity.getMenuManager()
        menuManager.search.setOnQueryTextListener(getSearchListener())
    }

    open fun getSearchListener(): SearchView.OnQueryTextListener {
        return object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(p0: String?): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        }
    }

}