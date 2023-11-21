package pet.openweatherapp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepository {
    val appID = "ea03f85ba1cdeddd17dcb0e3966500d5"

    companion object {
        val api = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenWeatherMapAPI::class.java)
    }

    suspend fun getCurrentWeather(city: String, countryCode: String): WeatherForecast =
        withContext(Dispatchers.IO) {
            api.getWeather("$city,$countryCode", appID).let {
                WeatherForecast(
                    cityName = it.cityName,
                    countryCode = it.sys.countryCode,
                    weatherDescription = it.weather.first().description,
                    temperature = it.main.temp,
                    humidity = it.main.humidity
                )
            }
        }
}

// Промежуточный класс для работы с API