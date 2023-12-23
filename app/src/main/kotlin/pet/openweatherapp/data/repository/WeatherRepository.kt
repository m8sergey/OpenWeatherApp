package pet.openweatherapp.data.repository

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import pet.openweatherapp.BuildConfig
import pet.openweatherapp.OpenWeatherApp
import pet.openweatherapp.domain.Location
import pet.openweatherapp.domain.Weather
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
                    GsonBuilder().registerTypeAdapter(
                        LocalDateTime::class.java,
                        JsonDeserializer { json, _, _ ->
                            Instant
                                .ofEpochSecond(json.asJsonPrimitive.asLong)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                        }
                    ).create()
                )
            ).build().create(OpenWeatherMapAPI::class.java)
    }

    private val locationDao = OpenWeatherApp.db.locationDAO()
    private val weatherDao = OpenWeatherApp.db.weatherDAO()

    private fun WeatherResponse.toWeather(countryCode: String = "", cityName: String = "") =
        Weather(
            cityName = this.cityName ?: cityName,
            countryCode = sys.countryCode ?: countryCode,
            weatherDescription = weather.first().description,
            temperature = main.temp,
            humidity = main.humidity,
            dateTime = date
        )

    private val currentLang get() = Locale.getDefault().language

    suspend fun getCurrentWeather(countryCode: String, city: String): Weather =
        withContext(Dispatchers.IO) {
            api.getWeather("$city,$countryCode", currentLang, BuildConfig.API_KEY)
                .toWeather()
                .also {
                    locationDao.insertLocation(DBLocation(Location(it.countryCode, it.cityName)))
                }
        }

    suspend fun getForecast(countryCode: String, city: String): List<Weather> =
        withContext(Dispatchers.IO) {
            api.getForecast("$city,$countryCode", currentLang, BuildConfig.API_KEY).run {
                list.map { it.toWeather(this.city.country, this.city.name) }
                    .also { weatherDao.insertWeather(it.map { weather -> DBWeather(weather) }) }
            }
        }

    suspend fun searchLocations(countryQuery: String, cityQuery: String) =
        withContext(Dispatchers.IO) {
            locationDao.searchLocations(countryQuery, cityQuery)
                .map { it.location }
        }

    suspend fun getHistoricalWeather(
        latitude: Double,
        longitude: Double,
        startDateTime: LocalDateTime
    ): List<Weather> =
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