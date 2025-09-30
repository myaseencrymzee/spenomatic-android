package com.crymzee.spenomatic.adapter

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import com.crymzee.spenomatic.utils.PlaceApi

class PlaceAutoSuggestAdapter(context: Context, resource: Int) :
    ArrayAdapter<Any?>(context, resource), Filterable {
    var results: ArrayList<String>? = null

    var placeApi: PlaceApi = PlaceApi()

    override fun getCount(): Int {
        return results!!.size
    }

    override fun getItem(pos: Int): String {
        return results!![pos]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null) {
                    results = placeApi.autoComplete(constraint.toString())
                    filterResults.values = results
                    filterResults.count = results?.size ?: 0
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

}
