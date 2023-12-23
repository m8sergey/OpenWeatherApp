package pet.openweatherapp.ui.weather

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pet.openweatherapp.data.repository.WeatherRepository
import pet.openweatherapp.domain.Weather

class WeatherViewModel : ViewModel() {
    private val repository = WeatherRepository()

    val forecast = MutableLiveData<List<Weather>>()
    val isLoading = MutableLiveData(true)
    val notFound = MutableLiveData<String>()

    fun getForecast(countryCode: String, city: String) {
        viewModelScope.launch {
            try {
                forecast.value = repository.getForecast(countryCode, city)
            } catch (e: Exception) {
                Log.wtf(javaClass.simpleName, e)
                notFound.value = e.message
            }
            isLoading.value = false
        }
    }

}