package pet.openweatherapp.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocation(location: DBLocation): Long

    @Delete
    suspend fun deleteLocation(location: DBLocation)

    @Query("select * from Location")
    suspend fun getAllLocation(): List<DBLocation>

    @Query("select * from Location where countryCode like '%'||:query||'%'")
    suspend fun searchByCountry(query: String): List<DBLocation>
}
