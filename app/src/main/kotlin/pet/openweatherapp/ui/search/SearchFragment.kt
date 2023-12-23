package pet.openweatherapp.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import pet.openweatherapp.R
import pet.openweatherapp.domain.Location
import pet.openweatherapp.databinding.FragmentSearchBinding
import pet.openweatherapp.ui.weather.WeatherFragment

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private val viewModel: SearchViewModel by viewModels()

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
        viewModel.isLoading.observe(this.viewLifecycleOwner) {
            binding.loader.isVisible = it
            binding.btnSearch.isVisible = !it
        }

        viewModel.error.observe(this.viewLifecycleOwner) {
            Snackbar.make(
                binding.root,
                getString(R.string.hot_found_message).format(it),
                Snackbar.LENGTH_SHORT
            ).show()
        }

        viewModel.currentWeather.observe(this.viewLifecycleOwner) { weather ->
            parentFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(
                    R.id.fragment_container,
                    WeatherFragment().also { f ->
                        f.arguments = Bundle().also { b ->
                            b.putParcelable(WEATHER_KEY, weather)
                        }
                    }
                ).addToBackStack(null).commit()
        }

        binding.btnSearch.setOnClickListener {
            val countryCode = binding.countryCodeInput.text.toString()
            val city = binding.cityInput.text.toString()

            if (city.isNotBlank() &&
                !city.contains(Regex(".*\\d+.*")) &&
                countryCode.isNotBlank() &&
                !countryCode.contains(Regex(".*\\d+.*"))
            ) {
                viewModel.loadCurrentWeather(countryCode, city)
            } else {
                val errorMessage = getString(R.string.invalid_input_message)

                if (city.isBlank() || city.contains(Regex(".*\\d+.*")))
                    binding.cityInputLayout.error = errorMessage
                if (countryCode.isBlank() || countryCode.contains(Regex(".*\\d+.*")))
                    binding.countryCodeInputLayout.error = errorMessage
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

        createAdapter(listOf(getString(R.string.loading))) // Autocomplete from first character requires default value

        viewModel.locations.observe(this@SearchFragment.viewLifecycleOwner) { locations ->
            createAdapter(locations.map(map).distinct())
        }

        addTextChangedListener {
            viewModel.searchLocations(
                countryQuery = binding.countryCodeInput.text.toString().trim(),
                cityQuery = binding.cityInput.text.toString().trim()
            )
        }
    }
}