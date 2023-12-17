package pet.openweatherapp.data

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class Weather(
    val cityName: String,
    val countryCode: String,
    val weatherDescription: String,
    val temperature: Double,
    val humidity: Int,
    val dateTime: LocalDateTime,
    val icon: Bitmap?
) : Parcelable

data class Location(
    val countryCode: String,
    val cityName: String
)