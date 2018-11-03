package com.dev.epicture.epicture.home

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.dev.epicture.epicture.home.fragment.FavoritesFragment
import com.dev.epicture.epicture.home.fragment.ImagesFragment
import com.dev.epicture.epicture.R
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        when (item.itemId) {
            R.id.navigation_images -> {
                val newFragment = ImagesFragment()
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragmentLayout, newFragment)
                transaction.addToBackStack(null)
                transaction.commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorites -> {
                val newFragment = FavoritesFragment()
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragmentLayout, newFragment)
                transaction.addToBackStack(null)
                transaction.commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_upload -> {
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

}
