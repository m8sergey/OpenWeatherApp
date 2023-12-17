package pet.openweatherapp

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.room.Room
import pet.openweatherapp.data.db.WeatherDatabase

class OpenWeatherApp : Application() {
    companion object {
        lateinit var db: WeatherDatabase
            private set
        lateinit var networkConnection: ConnectivityManager
    }

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(
            applicationContext,
            WeatherDatabase::class.java,
            "weather_db"
        ).build()

        networkConnection = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

//    if ((context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetwork != null) {
//        Log.wtf("Net", "Connected")
//    } else {
//        Log.wtf("Net", "Disconnected")
//    }

}

