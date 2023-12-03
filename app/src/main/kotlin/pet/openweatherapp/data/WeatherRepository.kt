package pet.openweatherapp.data

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pet.openweatherapp.OpenWeatherApp
import pet.openweatherapp.data.api.OpenWeatherMapAPI
import pet.openweatherapp.data.api.WeatherResponse
import pet.openweatherapp.data.db.DBLocation
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class WeatherRepository {
    companion object {
        private val appID = "1ee7cd666f11e032c15527cac5e472d0"

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

    private val weatherDao = OpenWeatherApp.db.weatherDAO()

    private fun WeatherResponse.toWeather() =
        Weather(
            cityName = cityName.orEmpty(),
            countryCode = sys.countryCode.orEmpty(),
            weatherDescription = weather.first().description,
            temperature = main.temp,
            humidity = main.humidity,
            dateTime = date
        )

    suspend fun getCurrentWeather(city: String, countryCode: String): Weather =
        withContext(Dispatchers.IO) {
            api.getWeather("$city,$countryCode", appID)
                .toWeather()
                .also {
                    weatherDao.insertLocation(DBLocation(Location(it.countryCode, it.cityName)))
                        .also {
                            Log.d("Repository", "Insert DBLocation with id=$it")
                        }
                }
        }

    suspend fun searchCountries(query: String) = withContext(Dispatchers.IO) {
        weatherDao.searchByCountry(query).map { it.location }
    }

    suspend fun getForecast(city: String?, countryCode: String?): List<Weather> =
        withContext(Dispatchers.IO) {
            api.getForecast("$city,$countryCode", appID).list.map { it.toWeather() }
        }
}

// Промежуточный класс для работы с API