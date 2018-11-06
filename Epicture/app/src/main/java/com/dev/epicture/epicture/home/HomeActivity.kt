package com.dev.epicture.epicture.home

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.home.fragment.FavoritesFragment
import com.dev.epicture.epicture.home.fragment.GalleryFragment
import com.dev.epicture.epicture.home.fragment.ImagesFragment
import com.dev.epicture.epicture.home.fragment.UploadFragment
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : AppCompatActivity() {

    var actionMenu : Menu? = null
    var searchView: SearchView? = null

    companion object {
        val galleryFragment = ImagesFragment()
        val favoritesFragment = FavoritesFragment()
        val searchFragment = FavoritesFragment()
        val uploadFragment = UploadFragment()
    }

    private fun setFragment(fragment: GalleryFragment) {

        actionMenu?.findItem(R.id.action_refresh)?.isVisible = false
        actionMenu?.findItem(R.id.action_favorite)?.isVisible = false
        actionMenu?.findItem(R.id.action_delete)?.isVisible = false
        actionMenu?.findItem(R.id.action_cancel)?.isVisible = false

        searchView?.setOnQueryTextListener(fragment.getSearchListener())

        val t = supportFragmentManager.beginTransaction()
        t.replace(R.id.contentFragment, fragment)
        t.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId) {
            android.R.id.home -> {
                findViewById<DrawerLayout>(R.id.drawer_layout).openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        when (item.itemId) {
            R.id.navigation_images -> {
                setFragment(galleryFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
                setFragment(favoritesFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                setFragment(searchFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_upload -> {
                setFragment(uploadFragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.

        menuInflater.inflate(R.menu.gallery_menu, menu)
        actionMenu = menu

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView!!.maxWidth = Integer.MAX_VALUE

        setFragment(ImagesFragment())
        return true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val toolbar = findViewById<Toolbar>(R.id.action_bar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_secondary)
        }

    }

}
