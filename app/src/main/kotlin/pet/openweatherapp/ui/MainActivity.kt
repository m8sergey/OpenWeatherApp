package pet.openweatherapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import pet.openweatherapp.R
import pet.openweatherapp.ui.search.SearchFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fragment_container, SearchFragment())
            .commit()
    }
}