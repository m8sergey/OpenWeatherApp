package pet.openweatherapp.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocation(location: DBLocation)

    @Delete
    suspend fun deleteLocation(location: DBLocation)

    @Query("select * from Location")
    suspend fun getAllLocation(): List<DBLocation>

    @Query(
        """
            select * from Location 
            where countryCode like '%'||:countryQuery||'%' 
            and cityName like '%'||:cityQuery||'%'
        """
    )
    suspend fun searchLocations(countryQuery: String, cityQuery: String): List<DBLocation>
}
