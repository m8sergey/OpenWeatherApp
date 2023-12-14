package pet.openweatherapp.ui

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.annotation.LayoutRes

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
// Этот кастомный адаптер создан только для того что отключить дефолтный фильтер адаптера