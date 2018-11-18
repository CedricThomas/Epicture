package com.dev.epicture.epicture.activities.home.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.SearchView
import com.dev.epicture.epicture.activities.home.HomeActivity

import com.dev.epicture.epicture.activities.home.HomeActivity.ActionMenuManager

/**
 * Abstract Fragment binding menuManager and search method (only usable in HomeActvity)
 */
open class GalleryFragment : Fragment() {

    lateinit var menuManager: ActionMenuManager

    /**
     *  Link searchView with the gallery fragment searchListener and get menuManager from Home activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = (activity as HomeActivity)
        menuManager = activity.getMenuManager()
        menuManager.search.setOnQueryTextListener(getSearchListener())
    }

    /**
     *  Allow to use an unified searchListener method
     */
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