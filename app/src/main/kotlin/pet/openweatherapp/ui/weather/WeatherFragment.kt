package pet.openweatherapp.ui.weather

import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import pet.openweatherapp.R
import pet.openweatherapp.domain.Weather
import pet.openweatherapp.databinding.FragmentWeatherForecastBinding
import pet.openweatherapp.ui.search.SearchFragment

class WeatherFragment : Fragment() {
    private lateinit var binding: FragmentWeatherForecastBinding
    private val viewModel: WeatherViewModel by viewModels()

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
                requireArguments().getParcelable(SearchFragment.WEATHER_KEY, Weather::class.java)
            else
                requireArguments().getParcelable(SearchFragment.WEATHER_KEY)

        val searchItem = requireArguments().getString(SearchFragment.SEARCH_KEY, "")
        Log.wtf(javaClass.simpleName, searchItem)

        currentWeather?.let { weather ->
            binding.locationName.text = getString(R.string.location_name_template).format(weather.cityName, weather.countryCode)
            binding.weatherDescription.text = weather.weatherDescription.replaceFirstChar { it.titlecase() }
            binding.temperature.text = getString(R.string.temperature_template).format(weather.temperature)
            binding.humidity.text = getString(R.string.humidity_template).format(weather.humidity)
            binding.currentWeatherIcon.setImageBitmap(weather.icon)

            viewModel.isLoading.observe(this.viewLifecycleOwner) {
                binding.loader.isVisible = it
            }

            viewModel.notFound.observe(this.viewLifecycleOwner) {
                Snackbar.make(
                    binding.root,
                    R.string.hot_found_message,
                    Snackbar.LENGTH_SHORT
                ).show()
            }

            viewModel.forecast.observe(this.viewLifecycleOwner) {
                adapter.updateForecast(it)
            }

            viewModel.getForecast(weather.countryCode, weather.cityName)

        }

    }
}
