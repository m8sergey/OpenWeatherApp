package pet.openweatherapp.data

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pet.openweatherapp.BuildConfig
import pet.openweatherapp.OpenWeatherApp
import pet.openweatherapp.data.api.OpenWeatherMapAPI
import pet.openweatherapp.data.api.WeatherResponse
import pet.openweatherapp.data.db.DBLocation
import pet.openweatherapp.data.db.DBWeather
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class WeatherRepository {
    companion object {
        private val api: OpenWeatherMapAPI = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(
                GsonConverterFactory.create(
                    // Конвертер может принимать доп. объект gson, который можно применить для конф. доп. параметров
                    // Для преобразования long в тип даты.
                    GsonBuilder().registerTypeAdapter(
                        LocalDateTime::class.java,
                        JsonDeserializer { json, _, _ ->
                            Instant // отпечаток во времени. осолютное время
                                .ofEpochSecond(json.asJsonPrimitive.asLong) // приметивный тип который приходит из json
                                .atZone(ZoneId.systemDefault()) //
                                .toLocalDateTime()
                        }
                    ).create()
                )
            )
            .build()
            .create(OpenWeatherMapAPI::class.java)
    }

    private val locationDao = OpenWeatherApp.db.locationDAO()
    private val weatheDao = OpenWeatherApp.db.weatherDAO()

    private fun WeatherResponse.toWeather(cityName: String = "", countryCode: String = "") =
        Weather(
            cityName = this.cityName ?: cityName,
            countryCode = sys.countryCode ?: countryCode,
            weatherDescription = weather.first().description,
            temperature = main.temp,
            humidity = main.humidity,
            dateTime = date
        )

    suspend fun getCurrentWeather(city: String, countryCode: String): Weather =
        withContext(Dispatchers.IO) {
            api.getWeather("$city,$countryCode", BuildConfig.API_KEY)
                .toWeather()
                .also {
                    locationDao.insertLocation(DBLocation(Location(it.countryCode, it.cityName)))
                }
        }

    suspend fun getForecast(city: String?, countryCode: String?): List<Weather> =
        withContext(Dispatchers.IO) {
            val response = api.getForecast("$city,$countryCode", BuildConfig.API_KEY)
            response.list.map {
                it.toWeather(response.city.name, response.city.country)
            }.also { weatheDao.insertWeather(it.map { DBWeather(it) }) }
        }

    suspend fun searchLocations(countryQuery: String, cityQuery: String) =
        withContext(Dispatchers.IO) {
            locationDao.searchLocations(countryQuery, cityQuery)
                .map { it.location }
        }
}

// Промежуточный класс для работы с API