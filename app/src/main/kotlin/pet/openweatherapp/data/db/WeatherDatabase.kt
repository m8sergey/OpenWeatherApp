package pet.openweatherapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DBLocation::class], version = 1)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDAO() : WeatherDAO
}