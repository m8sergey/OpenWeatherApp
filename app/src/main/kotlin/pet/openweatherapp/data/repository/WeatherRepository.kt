package pet.openweatherapp.data.repository

import android.graphics.BitmapFactory
import android.util.Log
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
import java.lang.Exception
import java.net.URL
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

const val API_URL = "https://api.openweathermap.org/data/2.5/"
const val IMAGE_URL = "https://openweathermap.org/img/wn/"

class WeatherRepository {
    companion object {
        private val api: OpenWeatherMapAPI = Retrofit.Builder()
            .baseUrl(API_URL)
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
            dateTime = date,
            icon = BitmapFactory.decodeStream(
                URL("$IMAGE_URL${weather.first().icon}@2x.png")
                    .openConnection().getInputStream()
            )
        )

    private val currentLang get() = Locale.getDefault().language

    suspend fun getCurrentWeather(countryCode: String, city: String): Weather =
        withContext(Dispatchers.IO) {
            api.getWeather("$city,$countryCode", currentLang, BuildConfig.API_KEY)
                .toWeather()
                .also {
                    locationDao.insertLocation(DBLocation(Location(it.countryCode, it.cityName)))
                }
            Log.wtf("now time", LocalDateTime.now().toString())
            Log.wtf("DB time", "${weatherDao.getLocationWeather(countryCode, city).first().weather.dateTime}")
            if (OpenWeatherApp.networkConnection.activeNetwork != null) {
                api.getWeather("$city,$countryCode", currentLang, BuildConfig.API_KEY)
                    .toWeather()
                    .also {
                        locationDao.insertLocation(DBLocation(Location(it.countryCode, it.cityName)))
                    }
            } else if (
                weatherDao.getLocationWeather(countryCode, city).first().weather.dateTime > LocalDateTime.now()
            ) {
                weatherDao.getLocationWeather(countryCode, city).first().weather
            } else {
                throw Exception()
            }
        }

    suspend fun getForecast(countryCode: String, city: String): List<Weather> =
        withContext(Dispatchers.IO) {
            if (OpenWeatherApp.networkConnection.activeNetwork != null) {
                api.getForecast("$city,$countryCode", currentLang, BuildConfig.API_KEY).run {
                    weatherDao.deleteLocationWeather(countryCode, city) // delete old data
                    list.map {
                        it.toWeather(this.city.name, this.city.country)
                    }.also { weather -> weatherDao.insertWeather(weather.map { DBWeather(it) }) }
                }
            } else if (weatherDao.getLocationWeather(countryCode, city).first().weather.dateTime > LocalDateTime.now()) {
                weatherDao.getLocationWeather(countryCode, city).map { it.weather }
            } else {
                throw Exception()
            }
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