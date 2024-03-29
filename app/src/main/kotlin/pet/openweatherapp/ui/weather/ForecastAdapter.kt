package pet.openweatherapp.ui.weather

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pet.openweatherapp.R
import pet.openweatherapp.domain.Weather
import pet.openweatherapp.databinding.ItemForecastBinding
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ForecastAdapter : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {
    private var forecast: List<Weather> = emptyList()

    fun updateForecast(forecast: List<Weather>) {
        this.forecast = forecast
        notifyDataSetChanged() // Updates the entire View
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        return ForecastViewHolder(
            ItemForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        holder.bind(forecast[position])
    }

    override fun getItemCount() = forecast.size

    class ForecastViewHolder(private val binding: ItemForecastBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object { // in the companion so that it would be created once
            private val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        // А specific pattern, or take the device settings
        // DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        }

        fun bind(weather: Weather) {
            binding.dateTime.text = weather.dateTime.format(formatter)
            binding.itemWeatherDescription.text = weather.weatherDescription.replaceFirstChar { it.titlecase() }
            binding.itemTemperature.text = binding.root
                .context
                .getString(R.string.temperature_template)
                .format(weather.temperature)
            binding.itemHumidity.text = binding.root
                .context
                .getString(R.string.humidity_template)
                .format(weather.humidity)
            binding.weatherIcon.setImageBitmap(weather.icon)
        }
    }
}
