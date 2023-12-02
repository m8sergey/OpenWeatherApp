package pet.openweatherapp.data

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class WeatherResponse(
    val weather: List<WeatherItem>,
    val main: Main,
    @SerializedName("name") val cityName: String?,
    val sys: Sys,
    @SerializedName("dt") val date: LocalDateTime
) {
    data class WeatherItem(val description: String, val icon: String)

    data class Main(val temp: Double, val humidity: Int)

    data class Sys(@SerializedName("country") val countryCode: String?)

}

data class ForecastResponse(val list: List<WeatherResponse>)