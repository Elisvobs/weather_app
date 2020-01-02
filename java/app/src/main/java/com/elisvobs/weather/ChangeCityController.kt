package com.elisvobs.weather

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity

import com.google.android.material.textfield.TextInputEditText

class ChangeCityController : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.city_changer)

        val queryCity = findViewById<TextInputEditText>(R.id.cityQuery)

        queryCity.setOnEditorActionListener { textView, i, keyEvent ->
            val newCity = queryCity.editableText.toString()
            val newCityIntent = Intent(this@ChangeCityController, WeatherController::class.java)

            // Adds what was entered in the EditText as an extra to the intent.
            newCityIntent.putExtra("City", newCity)

            // We started this activity for a result, so now we are setting the result.
            setResult(Activity.RESULT_OK, newCityIntent)

            // This destroys the ChangeCityController.
            finish()
            true
        }
    }

    fun backClick(view: View) {
        // Go back and destroy the ChangeCityController
        finish()
    }
}