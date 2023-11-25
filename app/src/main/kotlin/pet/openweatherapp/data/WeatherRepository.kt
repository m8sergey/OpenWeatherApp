package pet.openweatherapp.data

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class WeatherRepository {
    val appID = "1ee7cd666f11e032c15527cac5e472d0"

    companion object {
        val api: OpenWeatherMapAPI = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(
                GsonConverterFactory.create(
                    // Конвертер может принимать доп. объект gson, который можно применить для конф. доп. параметров
                    // Для преобразования long в тип даты.
                    GsonBuilder().registerTypeAdapter(
                        LocalDateTime::class.java,
                        JsonDeserializer{ json, _, _ ->
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

    private fun WeatherForecastResponse.toCurrentWeather() =
        WeatherForecast(
            cityName = cityName.orEmpty(),
            countryCode = sys.countryCode.orEmpty(),
            weatherDescription = weather.first().description,
            temperature = main.temp,
            humidity = main.humidity,
            dateTime = date
        )

    suspend fun getCurrentWeather(city: String, countryCode: String): WeatherForecast =
        withContext(Dispatchers.IO) {
            api.getWeather("$city,$countryCode", appID).toCurrentWeather()
        }

    suspend fun getForecast(city: String?, countryCode: String?): List<WeatherForecast> =
        withContext(Dispatchers.IO) {
            api.getForecast("$city,$countryCode", appID).list.map { it.toCurrentWeather() }
        }
}

// Промежуточный класс для работы с API