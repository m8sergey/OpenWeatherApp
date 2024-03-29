package pet.openweatherapp.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherMapAPI {

    @GET("weather?units=metric")
    suspend fun getWeather(
        @Query("q") location: String,
        @Query("lang") lang: String = "en",
        @Query("appid") appId: String
    ): WeatherResponse

    @GET("forecast?units=metric")
    suspend fun getForecast(
        @Query("q") location: String,
        @Query("lang") lang: String = "en",
        @Query("appid") appId: String
    ): ForecastResponse

    @GET("onecall/timemachine?units=metric")
    suspend fun getHistoricalData(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("dt") date: Long,
        @Query("lang") lang: String = "en",
        @Query("appid") appId: String,
    ): HistoricalWeatherResponse
}