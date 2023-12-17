package pet.openweatherapp.data

import android.graphics.BitmapFactory
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
import java.lang.Exception
import java.net.URL
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

const val API_URL = "https://api.openweathermap.org/data/2.5/"
const val IMAGE_URL = "https://openweathermap.org/img/wn/"

class WeatherRepository {
    companion object {
        private val api: OpenWeatherMapAPI = Retrofit.Builder()
            .baseUrl(API_URL)
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
            ).build().create(OpenWeatherMapAPI::class.java)
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
            dateTime = date,
            icon = BitmapFactory.decodeStream(
                URL("$IMAGE_URL${weather.first().icon}@2x.png")
                    .openConnection().getInputStream()
            )
        )

    suspend fun getCurrentWeather(city: String, countryCode: String): Weather =
        withContext(Dispatchers.IO) {
            Log.wtf("now time", LocalDateTime.now().toString())
            Log.wtf("DB time", "${weatheDao.getLocationWeather(countryCode, city).first().weather.dateTime}")
            if (OpenWeatherApp.networkConnection.activeNetwork != null) {
                api.getWeather("$city,$countryCode", BuildConfig.API_KEY)
                    .toWeather()
                    .also {
                        locationDao.insertLocation(DBLocation(Location(it.countryCode, it.cityName)))
                    }
            } else if (
                weatheDao.getLocationWeather(countryCode, city).first().weather.dateTime > LocalDateTime.now()
            ) {
                weatheDao.getLocationWeather(countryCode, city).first().weather
            } else {
                throw Exception()
            }
        }

    suspend fun getForecast(city: String?, countryCode: String?): List<Weather> =
        withContext(Dispatchers.IO) {
            if (OpenWeatherApp.networkConnection.activeNetwork != null) {
                api.getForecast("$city,$countryCode", BuildConfig.API_KEY).run {
                    weatheDao.deleteLocationWeather(countryCode ?: "", city ?: "") // delete old data
                    list.map {
                        it.toWeather(this.city.name, this.city.country)
                    }.also { weather -> weatheDao.insertWeather(weather.map { DBWeather(it) }) }
                }
            } else if (weatheDao.getLocationWeather(countryCode!!, city!!).first().weather.dateTime > LocalDateTime.now()) {
                weatheDao.getLocationWeather(countryCode, city).map { it.weather }
            } else {
                throw Exception()
            }
        }

    suspend fun searchLocations(countryQuery: String, cityQuery: String) =
        withContext(Dispatchers.IO) {
            locationDao.searchLocations(countryQuery, cityQuery)
                .map { it.location }
        }


}

// Промежуточный класс для работы с API