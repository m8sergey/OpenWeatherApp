package pet.openweatherapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import pet.openweatherapp.R
import pet.openweatherapp.data.WeatherRepository
import pet.openweatherapp.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private val scope = MainScope()
    private val repository = WeatherRepository()

    companion object {
        const val WEATHER_KEY = "weather"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnSearch.setOnClickListener {
            val city = binding.cityInput.text.toString()
            val countryCode = binding.countryCodeInput.text.toString()

            if (city.isNotBlank() &&
                !city.contains(Regex(".*\\d+.*")) &&
                countryCode.isNotBlank() &&
                !countryCode.contains(Regex(".*\\d+.*"))
            ) {
                scope.launch {
                    binding.btnSearch.isVisible = false
                    binding.loader.isVisible = true

                    try {
                        val currentWeather = repository.getCurrentWeather(city, countryCode)
                        parentFragmentManager.beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(
                                R.id.fragment_container,
                                WeatherForecastFragment().also { f ->
                                    f.arguments = Bundle().also { b ->
                                        b.putParcelable(WEATHER_KEY, currentWeather)
                                    }
                                }
                            ).addToBackStack(null).commit()
                    } catch (e: Exception) {
                        Log.wtf(javaClass.simpleName, e)
                        Snackbar.make(binding.root, R.string.hot_found_message, Snackbar.LENGTH_SHORT)
                            .show()
                    }

                    binding.loader.isVisible = false
                    binding.btnSearch.isVisible = true
                }
            } else {
                val errorMessage = getString(R.string.invalid_input_message)
                if (city.isBlank() ||
                    city.contains(Regex(".*\\d+.*"))
                ) binding.cityInputLayout.error = errorMessage
                if (countryCode.isBlank() ||
                    countryCode.contains(Regex(".*\\d+.*"))
                ) binding.countryCodeInputLayout.error = errorMessage
            }
        }

        binding.cityInput.addTextChangedListener {
            binding.cityInputLayout.isErrorEnabled = false
        }

        binding.countryCodeInput.addTextChangedListener {
            binding.countryCodeInputLayout.isErrorEnabled = false
            scope.launch {
                Log.d("WeatherFragment", repository.searchCountries(it.toString()).toString())
            }
        }
    }
}