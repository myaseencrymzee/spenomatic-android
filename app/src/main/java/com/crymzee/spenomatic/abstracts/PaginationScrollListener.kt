package com.example.flowit.abstracts

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class PaginationScrollListener
/**
 * Supporting only LinearLayoutManager for now.
 *
 * @param layoutManager
 */
    (var layoutManager: LinearLayoutManager, var page_size: Int) : RecyclerView.OnScrollListener() {

    val TAG = this.javaClass.name

    abstract fun isLastPage(): Boolean

    abstract fun isLoading(): Boolean

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val totalItemCount = layoutManager.itemCount
        val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()

        Log.d(TAG, "$lastVisibleItem : $page_size")

        if (!isLoading() && !isLastPage()) {
            if (lastVisibleItem >= totalItemCount - 1 && !(lastVisibleItem < page_size - 1)) {
                loadMoreItems()
            }
        }
    }

    abstract fun loadMoreItems()
}