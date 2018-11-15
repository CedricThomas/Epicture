package com.dev.epicture.epicture.activities.home

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.activities.home.fragment.FavoritesFragment
import com.dev.epicture.epicture.activities.home.fragment.GalleryFragment
import com.dev.epicture.epicture.activities.home.fragment.ImagesFragment
import com.dev.epicture.epicture.activities.login.LoginActivity
import com.dev.epicture.epicture.services.imgur.ImgurService
import com.dev.epicture.epicture.services.upload.UploadService
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : AppCompatActivity() {

    private var animation = false
    var addMenu: Boolean = false
        set(value) {
            if (value == addMenu || animation)
                return
            animation = true
            if (addMenu) {
                fade(fab_1, fab.height + 5F, 0.0F) {
                    animation = false
                }
                fade(fab_2, 2 * (fab.height + 5F), 0.0F) {
                    animation = false
                }
            } else {
                fade(fab_1, -fab.height - 5F, 1.0F) {
                    animation = false
                }
                fade(fab_2, 2 * (-fab.height - 5F), 1.0F) {
                    animation = false
                }
            }
            field = value
        }
    lateinit var actionMenu : Menu
    private lateinit var searchView: SearchView

    inner class ActionMenuManager(actionMenu : Menu) {

        val refresh: MenuItem = actionMenu.findItem(R.id.action_refresh)
        val favorite: MenuItem = actionMenu.findItem(R.id.action_favorite)
        val delete: MenuItem = actionMenu.findItem(R.id.action_delete)
        val cancel: MenuItem = actionMenu.findItem(R.id.action_cancel)
        val selectAll: MenuItem = actionMenu.findItem(R.id.action_select_all)
        val search: SearchView = actionMenu.findItem(R.id.action_search).actionView as SearchView

        init {

            if (!searchView.isIconified) {
                searchView.isIconified = true
            }

            refresh.isVisible = false
            favorite.isVisible = false
            delete.isVisible = false
            cancel.isVisible = false
            selectAll.isVisible = false
            search.visibility = View.INVISIBLE
        }

    }

    fun getMenuManager(): ActionMenuManager {
        return ActionMenuManager(actionMenu)
    }

    private fun setFragment(fragment: GalleryFragment) {
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
        }
        false
    }

    private val mOnDrawerItemSelectedListener = NavigationView.OnNavigationItemSelectedListener { item ->
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        when (item.itemId) {
            R.id.draw_images -> {
                setFragment(ImagesFragment())
                drawer.closeDrawers()
                return@OnNavigationItemSelectedListener true
            }
            R.id.draw_favorite -> {
                setFragment(FavoritesFragment())
                drawer.closeDrawers()
                return@OnNavigationItemSelectedListener true
            }
            R.id.draw_logout -> {
                ImgurService.deleteCredentials()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
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
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.maxWidth = Integer.MAX_VALUE

        // Fragment need thee actionMenu
        setFragment(ImagesFragment())
        return true
    }

    private fun fade(view: View?, dist: Float, alpha: Float, onAnimationEnd: () -> Unit) {
        view?.animate()
            ?.translationYBy(dist)
            ?.alpha(alpha)
            ?.setDuration(300)
            ?.setListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    onAnimationEnd()
                }
            })
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (addMenu)
            addMenu = !addMenu
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val toolbar = findViewById<Toolbar>(R.id.action_bar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        nav_view.setNavigationItemSelectedListener(mOnDrawerItemSelectedListener)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_secondary)
        }

        fab.setOnClickListener {
            addMenu = !addMenu
        }
        fab_1.setOnClickListener { UploadService.choose(this) }
        fab_2.setOnClickListener { UploadService.camera(this) }
    }

    override fun onBackPressed() {
        if (!searchView.isIconified) {
            searchView.isIconified = true
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        UploadService.onActivityResult(this, requestCode, resultCode, data)
    }

}
