package pet.openweatherapp.data

import com.google.gson.annotations.SerializedName

data class WeatherForecastResponse(
    val weather: List<WeatherItem>,
    val main: Main,
    @SerializedName("name") val cityName: String,
    val sys: Sys
) {
    data class WeatherItem(val description: String, val icon: String)

    data class Main(val temp: Double, val humidity: Int)

    data class Sys(@SerializedName("country") val countryCode: String)

}