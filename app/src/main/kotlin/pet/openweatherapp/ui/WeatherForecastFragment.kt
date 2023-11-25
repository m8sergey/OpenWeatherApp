package pet.openweatherapp.ui

import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import pet.openweatherapp.R
import pet.openweatherapp.data.WeatherForecast
import pet.openweatherapp.data.WeatherRepository
import pet.openweatherapp.databinding.FragmentWeatherForecastBinding

class WeatherForecastFragment : Fragment() {
    private lateinit var binding: FragmentWeatherForecastBinding
    private val repository = WeatherRepository()
    private val scope = MainScope()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeatherForecastBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ForecastAdapter()
        binding.forecastList.adapter = adapter

        val currentWeather =
            if (Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU)
                requireArguments().getParcelable(SearchFragment.WEATHER_KEY, WeatherForecast::class.java)
            else
                requireArguments().getParcelable(SearchFragment.WEATHER_KEY)

        currentWeather?.let {
            binding.locationName.text = getString(R.string.location_name_template).format(it.cityName, it.countryCode)
            binding.weatherDescription.text = it.weatherDescription.replaceFirstChar { it.titlecase() }
            binding.temperature.text = getString(R.string.temperature_template).format(it.temperature)
            binding.humidity.text = getString(R.string.humidity_template).format(it.humidity)

            scope.launch {
                try {
                    adapter.updateForcaset(repository.getForecast(it.cityName, it.countryCode))
                } catch (e: Exception) {
                    Log.wtf(javaClass.simpleName, e)
                    Snackbar.make(binding.root, R.string.hot_found_message, Snackbar.LENGTH_SHORT)
                        .show()
                }

                binding.loader.isVisible = false // gone
            }
        }




    }
}
