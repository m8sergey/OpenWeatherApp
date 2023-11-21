package pet.openweatherapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WeatherForecast(
    val cityName: String,
    val countryCode: String,
    val weatherDescription: String,
    val temperature: Double,
    val humidity: Int
) : Parcelable