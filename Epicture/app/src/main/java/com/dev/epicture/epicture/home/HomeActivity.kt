package com.dev.epicture.epicture.home

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.dev.epicture.epicture.home.fragment.FavoritesFragment
import com.dev.epicture.epicture.home.fragment.ImagesFragment
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.home.fragment.UploadFragment
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    var actionMenu : Menu? = null

    private fun setFragment(fragment: Fragment) {

        actionMenu?.findItem(R.id.action_refresh)?.isVisible = false
        actionMenu?.findItem(R.id.action_favorite)?.isVisible = false
        actionMenu?.findItem(R.id.action_delete)?.isVisible = false
        actionMenu?.findItem(R.id.action_cancel)?.isVisible = false

        val t = supportFragmentManager.beginTransaction()
        t.replace(R.id.contentFragment, fragment)
        t.commit()
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        when (item.itemId) {
            R.id.navigation_images -> {
                setFragment(ImagesFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
                setFragment(FavoritesFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                setFragment(FavoritesFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_upload -> {
                setFragment(UploadFragment())
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.images_menu, menu)
        actionMenu = menu
        setFragment(ImagesFragment())
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar = findViewById<Toolbar>(R.id.action_bar)
        setSupportActionBar(toolbar)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

}
