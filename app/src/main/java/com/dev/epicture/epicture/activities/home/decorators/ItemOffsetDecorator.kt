package com.dev.epicture.epicture.activities.home.decorators

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Basic recyclerview separator
 */
class ItemOffsetDecoration(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset)
    }
}