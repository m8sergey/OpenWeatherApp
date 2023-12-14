package pet.openweatherapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: List<DBWeather>)

    @Query(
        """
            delete from Weather 
            where countryCode = :countryCode and cityName = :cityName
        """
    )
    suspend fun deleteLocationWeather(countryCode: String, cityName: String)

    @Query(
        """
            select * from Weather 
            where countryCode = :countryCode and cityName = :cityName
            order by dateTime
        """
    )
    suspend fun getLocationWeather(countryCode: String, cityName: String): List<DBWeather>
}