package pet.openweatherapp.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import pet.openweatherapp.data.Location

@Entity(tableName = "Location", indices = [Index("countryCode", "cityName", unique = true)])
data class DBLocation(
    @Embedded val location: Location, // встраивает колонки из свойств класса Location
    @PrimaryKey(autoGenerate = true) val id: Long = 0 // -1 зарезервировано под отмену операции
)