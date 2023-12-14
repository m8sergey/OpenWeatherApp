package pet.openweatherapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pet.openweatherapp.R
import pet.openweatherapp.data.Location
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
                        Snackbar.make(
                            binding.root,
                            R.string.hot_found_message,
                            Snackbar.LENGTH_SHORT
                        )
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


        binding.countryCodeInput.apply {
            addTextChangedListener {
                binding.countryCodeInputLayout.isErrorEnabled = false
            }
            configureInput { it.countryCode }
        }

        binding.cityInput.apply {
            addTextChangedListener {
                binding.cityInputLayout.isErrorEnabled = false
            }
            configureInput { it.cityName }
        }
    }

    private fun MaterialAutoCompleteTextView.configureInput(map: (Location) -> String) {
        fun createAdapter(objects: List<String>) = setAdapter(
            SimpleAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                objects
            )
        )

        createAdapter(listOf(getString(R.string.loading))) // для того чтобы авт. дополнение работало с первого символа - нужно какое то дефолтное значени

        addTextChangedListener {
            scope.launch {
                repository.searchLocations(
                    countryQuery = binding.countryCodeInput.text.toString().trim(),
                    cityQuery = binding.cityInput.text.toString().trim()
                )
                    .map(map)
                    .distinct()
                    .let { createAdapter(it) }

            }
        }
    }
}