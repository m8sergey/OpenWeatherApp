package pet.openweatherapp.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import pet.openweatherapp.data.Location
import pet.openweatherapp.data.Weather

@Entity(
    tableName = "Location",
//    indices = [Index("countryCode", "cityName", unique = true)],
    primaryKeys = ["countryCode", "cityName"] // ключ сам по себе подразумевает что он уникальный
)
data class DBLocation(
    @Embedded val location: Location, // встраивает колонки из свойств класса Location
 //   @PrimaryKey(autoGenerate = true) val id: Long = 0 // -1 зарезервировано под отмену операции
)

@Entity(
    tableName = "Weather",
    indices = [Index("countryCode", "cityName", "dateTime", unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = DBLocation::class,
            parentColumns = ["countryCode", "cityName"],
            childColumns = ["countryCode", "cityName"],
            deferred = true,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DBWeather(
    @Embedded val weather: Weather,
    // do we really need id?
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)