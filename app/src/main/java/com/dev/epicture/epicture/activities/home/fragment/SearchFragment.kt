package com.dev.epicture.epicture.activities.home.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dev.epicture.epicture.MyApplication
import com.dev.epicture.epicture.R
import com.dev.epicture.epicture.activities.home.adapter.SearchFragmentItemAdapter
import com.dev.epicture.epicture.activities.home.decorators.ItemOffsetDecoration
import com.dev.epicture.epicture.services.imgur.ImgurService
import com.dev.epicture.epicture.services.imgur.models.*
import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlinx.android.synthetic.main.fragment_gallery_search.*


class SearchFragment : GalleryFragment() {

    private var images: ArrayList<PostModel> = ArrayList()
    private lateinit var adapter: SearchFragmentItemAdapter
    private lateinit var fragView: View
    private var loading: Boolean = false
    private var searching: Boolean = false
        set(value) {
            val old = field
            field = value
            if (value != old) {
                page = 0
                images.clear()
                loadActivePages(recycler_view, menuManager.supportActionBar?.title.toString())
            }
        }
    private var page: Int = 0

    // activate / deactivate selection mode on action bar
    private fun setSelectionMode(status: Boolean)  {
        menuManager.favorite.isVisible = status
        menuManager.cancel.isVisible = status
        menuManager.refresh.isVisible = !status
    }

    // Cancel selection in adapter
    private fun cancelSelection() {
        val selected = adapter.getSelection()
        selected.forEach { image ->
            image.selected = false
        }
        adapter.selecting = false
        setSelectionMode(false)
        adapter.notifyDataSetChanged()
    }

    // Favorite activation
    private fun activateFavorite() {
        menuManager.favorite.setOnMenuItemClickListener {
            val selected = adapter.getSelection()
            for (elem in selected)
                elem.favorite = !(elem.favorite!!)
            favorites(selected)
            cancelSelection()
            return@setOnMenuItemClickListener true
        }
    }

    // KillSearch Activation
    private fun activateKillSearch() {
        menuManager.kill_search.setOnMenuItemClickListener {
            menuManager.kill_search.isVisible = false
            menuManager.searchItem.isVisible = true
            menuManager.search.setQuery("", false)
            menuManager.supportActionBar?.title = ""
            cancelSelection()
            searching = false
            return@setOnMenuItemClickListener true
        }
    }

    // Reload Activation
    private fun activateReload(recyclerView: RecyclerView) {
        // Reload activation
        menuManager.refresh.isVisible = true
        menuManager.refresh.setOnMenuItemClickListener {
            loadActivePages(recyclerView, menuManager.supportActionBar?.title.toString())
            return@setOnMenuItemClickListener true
        }
    }

    // Cancel activation
    private fun activateCancelSelection() {
        menuManager.cancel.setOnMenuItemClickListener {
            cancelSelection()
            return@setOnMenuItemClickListener true
        }
    }

    // Infinite scroll activation
    private fun activateInfiniteScroll(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1) && !loading) {
                    val size = images.size
                    val callback: () -> Unit = {
                        if (images.size > size) {
                            page += 1
                        }
                        loading = false
                        activity?.runOnUiThread {
                            recyclerView.adapter?.notifyDataSetChanged()
                        }
                    }
                    loading = true
                    if (searching)
                        searchImage(menuManager.search.query.toString(), page + 1, callback = callback)
                    else
                        loadGallery(page + 1, callback = callback)
                }
            }
        })
    }

    // callback to activate Imgur functions (upvote downvote favorite)
    private fun activateViewActions(imgurAction: ImgurAction, model: PostModel) {
        val smartVote = {vote: String -> ImgurService.vote({}, {}, model.id!!, vote)}
        when (imgurAction) {
            ImgurAction.RESET_VOTE -> smartVote("veto")
            ImgurAction.DOWN -> smartVote("down")
            ImgurAction.UP -> smartVote("up")
            ImgurAction.FAVORITE -> favorites(arrayListOf(model))
        }
    }

    // load all active pages in images (with search and trends ) => c.f. searching
    private fun loadActivePages(recyclerView: RecyclerView, query: String) {
        if (loading)
            return
        images.clear()
        loading = true
        for (i in 0..page) {
            val callback = {
                if (i == page) {
                    activity?.runOnUiThread {
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                    loading = false
                }
            }
            if (searching)
                searchImage(query, i, callback = callback)
            else
                loadGallery(i, callback = callback)
        }
    }

    // configure RecyclerView and active actionBar actions
    private fun createRecyclerView() {

        adapter = SearchFragmentItemAdapter(images, context!!, {a, b -> activateViewActions(a, b)}) { adapter, model ->
            if (!adapter.selecting) {
                model.selected = true
                adapter.selecting = true
                setSelectionMode(true)
                adapter.notifyDataSetChanged()
            }
            true
        }
        val recyclerView = fragView.findViewById<RecyclerView>(R.id.recycler_view)
        val itemDecoration = ItemOffsetDecoration(5)
        recyclerView.addItemDecoration(itemDecoration)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        loadActivePages(recyclerView, "")
        activateCancelSelection()
        activateInfiniteScroll(recyclerView)
        activateReload(recyclerView)
        activateKillSearch()
        activateFavorite()
    }

    // configure recycler view and inflate view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragView = inflater.inflate(R.layout.fragment_gallery_search, container, false)
        createRecyclerView()
        return fragView
    }

    // load Trends
    private fun loadGallery(page: Int = 0, section: String = "hot", sort: String = "viral", window: String = "day", callback: () -> Unit = {}) {
        ImgurService.getGallery({ resp ->
            try {
                for (image in resp.data) {
                    images.add(PostUtils.elementToPost(image))
                }
            } catch (e : Exception) {
                MyApplication.printMessage("Failed to load images page $page")
            }
            callback()
        }, {
            MyApplication.printMessage("Failed to load images page $page")
            callback()
        }, page.toString(), section, sort, window)
    }


    // search Images
    private fun searchImage(query: String,
                            page: Int, sort: String = "time", window: String = "all", callback: () -> Unit = {}) {
        ImgurService.search({ resp ->
            try {
                for (image in resp.data) {
                    images.add(PostUtils.elementToPost(image))
                }
            } catch (e : Exception) {
                MyApplication.printMessage("Failed to load images page $page")
            }
            callback()
        }, {
            MyApplication.printMessage("Failed to load images page $page")
            callback()
        }, query, page.toString(), sort, window)
    }

    // delete select item from array
    private fun favorites(images: ArrayList<PostModel>) {
        for (image in images) {
            if (image.is_album)
                ImgurService.favoriteAlbum({}, {}, image.id!!)
            else
                ImgurService.favoriteImage({}, {}, image.id!!)
        }
    }

    // add a search listener custom
    override fun getSearchListener(): SearchView.OnQueryTextListener {
        return object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                menuManager.search.setQuery("", false)
                menuManager.search.clearFocus()
                menuManager.search.isIconified = true
                menuManager.supportActionBar?.setDisplayShowTitleEnabled(true)
                menuManager.supportActionBar?.title = query
                menuManager.kill_search.isVisible = true
                menuManager.searchItem.isVisible = false
                searching = true
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                return false
            }

        }
    }

}
