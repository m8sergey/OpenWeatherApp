package pet.openweatherapp.data

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherMapAPI {

    @GET("weather?units=metric") //?nits=metric - добавлен прямо сдесь т.к. этот query параметр не будет менятся
    suspend fun getWeather(
        @Query("q") location: String,
        @Query("appid") appId: String
    ): WeatherForecastResponse

    @GET("forecast?units=metric")
    suspend fun getForecast(
        @Query("q") location: String,
        @Query("appid") appId: String
    ): ForecastResponse


}