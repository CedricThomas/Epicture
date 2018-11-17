package com.dev.epicture.epicture.activities.home

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.util.Log
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.activities.home.fragment.FavoritesFragment
import com.dev.epicture.epicture.activities.home.fragment.GalleryFragment
import com.dev.epicture.epicture.activities.home.fragment.ImagesFragment
import com.dev.epicture.epicture.activities.home.fragment.SearchFragment
import com.dev.epicture.epicture.activities.login.LoginActivity
import com.dev.epicture.epicture.services.glide.GlideApp
import com.dev.epicture.epicture.services.imgur.ImgurService
import com.dev.epicture.epicture.services.upload.UploadService
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.drawer_header_layout.view.*
import android.support.v4.view.MenuItemCompat
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.SpinnerAdapter


class HomeActivity : AppCompatActivity() {

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
                setFragment(SearchFragment())
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private val mOnDrawerItemSelectedListener = NavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
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

    private var animation = false

    private var addMenu: Boolean = false
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

    private lateinit var actionMenu : Menu

    private lateinit var searchView: SearchView

    // ActionBar sharing class
    inner class ActionMenuManager(actionMenu: Menu, val supportActionBar: android.support.v7.app.ActionBar?) {

        val kill_search: MenuItem = actionMenu.findItem(R.id.action_kill_search)
        val refresh: MenuItem = actionMenu.findItem(R.id.action_refresh)
        val favorite: MenuItem = actionMenu.findItem(R.id.action_favorite)
        val delete: MenuItem = actionMenu.findItem(R.id.action_delete)
        val cancel: MenuItem = actionMenu.findItem(R.id.action_cancel)
        val search: SearchView = actionMenu.findItem(R.id.action_search).actionView as SearchView
        val spinner: Spinner = actionMenu.findItem(R.id.action_spinner).actionView as Spinner
        val searchItem: MenuItem = actionMenu.findItem(R.id.action_search)
        val spinnerItem: MenuItem = actionMenu.findItem(R.id.action_spinner)

        init {

            if (!searchView.isIconified) {
                searchView.isIconified = true
            }

            kill_search.isVisible = false
            refresh.isVisible = false
            favorite.isVisible = false
            delete.isVisible = false
            cancel.isVisible = false
            searchItem.isVisible = true
            search.visibility = View.INVISIBLE
            spinnerItem.isVisible = false
            spinner.visibility = View.INVISIBLE
            spinner.setSelection(0)
        }

    }

    // Allow Fragments to get actionMenu
    fun getMenuManager(): ActionMenuManager {
        return ActionMenuManager(actionMenu, supportActionBar)
    }

    // Change Fragment and reset actionBar
    private fun setFragment(fragment: GalleryFragment) {
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val t = supportFragmentManager.beginTransaction()
        t.replace(R.id.contentFragment, fragment)
        t.commit()
    }

    // Open drawer on click
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId) {
            android.R.id.home -> {
                findViewById<DrawerLayout>(R.id.drawer_layout).openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Setup actionBar with spinner and searchView
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.

        menuInflater.inflate(R.menu.gallery_menu, menu)
        actionMenu = menu

        val s : Spinner = menu.findItem(R.id.action_spinner).actionView as Spinner
        val mSpinnerAdapter : SpinnerAdapter = ArrayAdapter.createFromResource(supportActionBar?.themedContext!!, R.array.dropdown, android.R.layout.simple_spinner_dropdown_item)
        s.adapter = mSpinnerAdapter

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.maxWidth = Integer.MAX_VALUE

        return true
    }

    // Change Fragment after menu is prepared (mandatory)
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        setFragment(SearchFragment())
        return super.onPrepareOptionsMenu(menu)
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


    // Remove FAB on click
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (addMenu)
            addMenu = !addMenu
        return super.dispatchTouchEvent(ev)
    }

    // Configure actionBar
    private fun setupActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.action_bar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        nav_view.setNavigationItemSelectedListener(mOnDrawerItemSelectedListener)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_secondary)
        }
    }

    // Configure FAB
    private fun setupFAB() {
        fab.setOnClickListener {
            addMenu = !addMenu
        }
        fab_1.setOnClickListener { UploadService.choose(this) }
        fab_2.setOnClickListener { UploadService.camera(this) }

    }

    // Configure Drawer Avatar
    private fun setupAvatar() {
        ImgurService.getAvatar({it ->
            runOnUiThread {
                GlideApp.with(this).load(it.data.avatar).into(nav_view.getHeaderView(0).avatar)
                nav_view.getHeaderView(0).username.text = it.data.username
            }
        },{})
    }

    // Call setup methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupActionBar()
        setupFAB()
        setupAvatar()
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
