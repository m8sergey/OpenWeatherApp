package pet.openweatherapp.data

import android.graphics.BitmapFactory
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

const val API_URL = "https://api.openweathermap.org/data/2.5/"
const val IMAGE_URL = "https://openweathermap.org/img/wn/"
const val APP_ID = "1ee7cd666f11e032c15527cac5e472d0"

class WeatherRepository {
    companion object {
        val api: OpenWeatherMapAPI = Retrofit.Builder()
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

    private fun WeatherResponse.toCurrentWeather() =
        WeatherForecast(
            cityName = cityName.orEmpty(),
            countryCode = sys.countryCode.orEmpty(),
            weatherDescription = weather.first().description,
            temperature = main.temp,
            humidity = main.humidity,
            dateTime = date,
            icon = BitmapFactory.decodeStream(
                URL("$IMAGE_URL${weather.first().icon}@2x.png")
                    .openConnection().getInputStream()
            )
        )

    suspend fun getCurrentWeather(city: String, countryCode: String): WeatherForecast =
        withContext(Dispatchers.IO) {
            api.getWeather("$city,$countryCode", APP_ID).toCurrentWeather()
        }

    suspend fun getForecast(city: String, countryCode: String): List<WeatherForecast> =
        withContext(Dispatchers.IO) {
            api.getForecast("$city,$countryCode", APP_ID).list.map { it.toCurrentWeather() }
        }
}

// Промежуточный класс для работы с API