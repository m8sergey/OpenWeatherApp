package pet.openweatherapp.ui.search

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.annotation.LayoutRes

// This custom adapter was created only to disable the default adapter filter
class SimpleAdapter(
    context: Context,
    @LayoutRes resource: Int,
    objects: List<String>
) : ArrayAdapter<String>(context, resource, objects) {
    override fun getFilter() = object : Filter() {
        override fun performFiltering(constraint: CharSequence?) = FilterResults()

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            notifyDataSetChanged()
        }
    }
}