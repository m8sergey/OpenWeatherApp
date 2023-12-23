package pet.openweatherapp.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import pet.openweatherapp.domain.Location
import pet.openweatherapp.domain.Weather

@Entity(
    tableName = "Location",
    primaryKeys = ["countryCode", "cityName"]
)
data class DBLocation(
    @Embedded val location: Location
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
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)