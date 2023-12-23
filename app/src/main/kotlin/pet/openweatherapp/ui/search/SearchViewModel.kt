package pet.openweatherapp.ui.search

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pet.openweatherapp.data.repository.WeatherRepository
import pet.openweatherapp.domain.Location
import pet.openweatherapp.domain.Weather
import pet.openweatherapp.ui.utils.SingleLineEvent

class SearchViewModel : ViewModel() {
    private val repository = WeatherRepository()

    val currentWeather = SingleLineEvent<Weather>()
    val locations = MutableLiveData<List<Location>>()
    val isLoading = MutableLiveData(false)
    val error = SingleLineEvent<String>()

    fun loadCurrentWeather(countryConde: String, city: String) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                currentWeather.setValue(repository.getCurrentWeather(countryConde, city))
            } catch (e: Exception) {
                Log.wtf(this@SearchViewModel.javaClass.simpleName, "Cannot load date: $e")
                error.setValue(e.message ?: "Error")
            }
            isLoading.value = false
        }
    }

    fun searchLocations(countryQuery: String, cityQuery: String) {
        viewModelScope.launch {
            locations.value = repository.searchLocations(countryQuery, cityQuery)
        }
    }
}
