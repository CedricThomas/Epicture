package com.dev.epicture.epicture.home

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.dev.epicture.epicture.home.fragment.FavoritesFragment
import com.dev.epicture.epicture.home.fragment.ImagesFragment
import com.dev.epicture.epicture.R
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    private fun setFragment(fragment: Fragment) {
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
            R.id.navigation_search -> {
                setFragment(FavoritesFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_upload -> {
                setFragment(FavoritesFragment())
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            // User chose the "Settings" item, show the app settings UI...
            true
        }

        R.id.action_favorite -> {
            // User chose the "Favorite" action, mark the current item
            // as a favorite...
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.images_menu, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar = findViewById<Toolbar>(R.id.action_bar)
        setSupportActionBar(toolbar)
        setFragment(ImagesFragment())
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)


    }

}
