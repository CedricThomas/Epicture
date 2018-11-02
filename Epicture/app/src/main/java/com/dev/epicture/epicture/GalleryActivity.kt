package com.dev.epicture.epicture

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_gallery.*

class GalleryActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        when (item.itemId) {
            R.id.navigation_home -> {
                val newFragment = GalleryFragment()
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragmentLayout, newFragment)
                transaction.addToBackStack(null)
                transaction.commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                val newFragment = FavoriteGalleryFragment()
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragmentLayout, newFragment)
                transaction.addToBackStack(null)
                transaction.commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

}
