package pet.openweatherapp.data

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
import java.util.Locale

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
    private val weatherDao = OpenWeatherApp.db.weatherDAO()

    private fun WeatherResponse.toWeather(cityName: String = "", countryCode: String = "") =
        Weather(
            cityName = this.cityName ?: cityName,
            countryCode = sys.countryCode ?: countryCode,
            weatherDescription = weather.first().description,
            temperature = main.temp,
            humidity = main.humidity,
            dateTime = date
        )

    private val currentLang get() = Locale.getDefault().language

    suspend fun getCurrentWeather(city: String, countryCode: String): Weather =
        withContext(Dispatchers.IO) {
            api.getWeather("$city,$countryCode", currentLang, BuildConfig.API_KEY)
                .toWeather()
                .also {
                    locationDao.insertLocation(DBLocation(Location(it.countryCode, it.cityName)))
                }
        }

    suspend fun getForecast(city: String?, countryCode: String?): List<Weather> =
        withContext(Dispatchers.IO) {
            val response = api.getForecast("$city,$countryCode", currentLang, BuildConfig.API_KEY)
            response.list.map {
                it.toWeather(response.city.name, response.city.country)
            }.also { weatherDao.insertWeather(it.map { DBWeather(it) }) }
        }

    suspend fun searchLocations(countryQuery: String, cityQuery: String) =
        withContext(Dispatchers.IO) {
            locationDao.searchLocations(countryQuery, cityQuery)
                .map { it.location }
        }

    suspend fun getHistoricalWeather(latitude: Double, longitude: Double, startDateTime: LocalDateTime): List<Weather> =
        withContext(Dispatchers.IO) {
            List(9) { (it + 1) * 3 } // offsets in hours: 3, 6, 9, ...
                .map { startDateTime.minusHours(it.toLong()) }
                .map {
                    val second = it.atZone(ZoneId.systemDefault()).toEpochSecond()
                    async {
                        api.getHistoricalData(
                            latitude,
                            longitude,
                            second,
                            currentLang,
                            BuildConfig.API_KEY
                        )
                    }
                }.awaitAll()
                .map { /* TODO map to Weather entity */ }
            emptyList()
        }
}

// Промежуточный класс для работы с API