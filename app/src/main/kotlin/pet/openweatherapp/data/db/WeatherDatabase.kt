package pet.openweatherapp.data.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime

@Database(
    entities = [DBLocation::class, DBWeather::class],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 2, to = 3, spec = WeatherDatabase.Migration1to2Spec::class)
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

        @TypeConverter
        fun stringToBitMap(bitMapString: String?): Bitmap? {
            val bytesArray = Base64.decode(bitMapString, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytesArray, 0, bytesArray.size)
        }

        @TypeConverter
        fun bitMapToString(bitMap: Bitmap?): String? {
            val boas = ByteArrayOutputStream()
            bitMap?.compress(Bitmap.CompressFormat.PNG, 100, boas)
            return Base64.encodeToString(boas.toByteArray(), Base64.DEFAULT)
        }
    }

    // Migrations (explicitly describe how to perform schema modifications)
    @DeleteColumn(tableName = "Location", columnName = "id")
    class Migration1to2Spec : AutoMigrationSpec
}