package pet.openweatherapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pet.openweatherapp.R
import pet.openweatherapp.data.WeatherForecast
import pet.openweatherapp.databinding.ItemForecastBinding
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ForecastAdapter : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {
    private var forecast: List<WeatherForecast> = emptyList()

    fun updateForecast(forecast: List<WeatherForecast>) {
        this.forecast = forecast
        notifyDataSetChanged() // обновляет всю view
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
        companion object { // в компаньоне что бы создался один раз
            private val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        // Можно указать конктретный паттер самостоятельно, либо взять настройки устройсва
        //DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        }

        fun bind(weather: WeatherForecast) {
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
