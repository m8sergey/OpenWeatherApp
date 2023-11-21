package pet.openweatherapp.ui

import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import pet.openweatherapp.R
import pet.openweatherapp.data.WeatherForecast
import pet.openweatherapp.databinding.FragmentWeatherForecastBinding

class WeatherForecastFragment : Fragment() {
    private lateinit var binding: FragmentWeatherForecastBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeatherForecastBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val currentWeather =
            if (Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU)
                requireArguments().getParcelable(SearchFragment.WEATHER_KEY, WeatherForecast::class.java)
            else
                requireArguments().getParcelable(SearchFragment.WEATHER_KEY)
        currentWeather?.let {
            binding.locationName.text =
                getString(R.string.location_name_template).format(it.cityName, it.countryCode)
            binding.weatherDescription.text =
                it.weatherDescription.replaceFirstChar { it.titlecase() }
            binding.temperature.text =
                getString(R.string.temperature_template).format(it.temperature)
            binding.humidity.text =
                getString(R.string.humidity_template).format(it.humidity)

        }
    }
}
