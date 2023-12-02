package pet.openweatherapp.data

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class WeatherForecast(
    val cityName: String,
    val countryCode: String,
    val weatherDescription: String,
    val temperature: Double,
    val humidity: Int,
    val dateTime: LocalDateTime,
    val icon: Bitmap
) : Parcelable