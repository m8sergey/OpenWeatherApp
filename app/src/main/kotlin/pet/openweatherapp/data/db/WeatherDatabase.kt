package pet.openweatherapp.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import pet.openweatherapp.data.db.dao.LocationDAO
import pet.openweatherapp.data.db.dao.WeatherDAO
import java.time.LocalDateTime

@Database(
    entities = [DBLocation::class, DBWeather::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = WeatherDatabase.Migration1to2Spec::class)
    ]
)
@TypeConverters(WeatherDatabase.Converters::class)
abstract class WeatherDatabase : RoomDatabase() {
    // Data Access Objects
    abstract fun locationDAO(): LocationDAO
    abstract fun weatherDAO(): WeatherDAO

    // Type converters (because Room sometimes doesn't know how to handle complex types
    object Converters {
        @TypeConverter
        fun stringToDateTime(dateTimeString: String?): LocalDateTime? =
            dateTimeString?.let { LocalDateTime.parse(it) }

        @TypeConverter
        fun dateTimeToString(dateTime: LocalDateTime?): String? =
            dateTime?.toString()
    }

    // Migrations (explicitly describe how to perform schema modifications)
    @DeleteColumn(tableName = "Location", columnName = "id")
    class Migration1to2Spec : AutoMigrationSpec
}