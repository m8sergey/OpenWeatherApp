package pet.openweatherapp

import android.app.Application
import androidx.room.Room
import pet.openweatherapp.data.db.WeatherDatabase

class OpenWeatherApp : Application() {
    companion object {
        lateinit var db: WeatherDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(
            applicationContext,
            WeatherDatabase::class.java,
            "weather_db"
        ).build()
    }
}

