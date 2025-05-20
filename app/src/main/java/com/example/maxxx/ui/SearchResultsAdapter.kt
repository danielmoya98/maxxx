package com.example.maxxx.ui

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.maxxx.R
import com.example.maxxx.databinding.ItemSearchResultBinding
import com.example.maxxx.ui.model.Place

class SearchResultsAdapter(
    private var places: List<Place>,
    private val onItemClick: (Place) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    private var currentQuery: String = ""

    inner class ViewHolder(private val binding: ItemSearchResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(place: Place, query: String) {
            // Resaltar coincidencia en el nombre
            val nameSpannable = SpannableString(place.name)
            val startIndex = place.name.indexOf(query, ignoreCase = true)

            if (query.isNotBlank() && startIndex >= 0) {
                val endIndex = startIndex + query.length
                val highlightColor = ContextCompat.getColor(binding.root.context, R.color.teal_200)
                nameSpannable.setSpan(
                    ForegroundColorSpan(highlightColor),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                nameSpannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            binding.placeName.text = nameSpannable
            binding.placeAddress.text = place.address

            binding.root.setOnClickListener {
                onItemClick(place)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = places.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(places[position], currentQuery)
    }

    fun updateData(newPlaces: List<Place>, query: String) {
        places = newPlaces
        currentQuery = query
        notifyDataSetChanged()
    }
}
