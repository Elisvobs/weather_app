package com.elisvobs.weather_app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams

import org.json.JSONObject

import org.jetbrains.annotations.Nullable

class WeatherController : AppCompatActivity() {
    internal val LOGCAT_TAG = "Weather App"
    // Set LOCATION_PROVIDER here and use LocationManager.NETWORK_PROVIDER on physical devices
    internal val LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER

    // Request Code for permission request callback
    private val REQUEST_CODE = 123
    // Request code for starting new activity for result callback
    private val NEW_CITY_CODE = 456

    // Base URL for the OpenWeatherMap API. More secure https is a premium feature =(
    private val WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather"
    // App ID to use OpenWeather data
    private val APP_ID = "88db9bdabecb70ea8373a3e2753d17b5"

    // Time between locationLabel updates (5000 milliseconds or 5 seconds)
    private val MIN_TIME: Long = 5000
    // Distance between locationLabel updates (1000m or 1km)
    private val MIN_DISTANCE = 1000f
    internal var useLocation = true

    // add locationLabel manager
    internal var locationManager: LocationManager? = null
    // add locationListener
    internal lateinit var locationListener: LocationListener

    internal lateinit var weatherImage: ImageView
    internal lateinit var temperatureLabel: TextView
    internal lateinit var locationLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weather_controller)

        weatherImage = findViewById(R.id.weatherSymbol)
        temperatureLabel = findViewById(R.id.tempText)
        locationLabel = findViewById(R.id.locationText)
    }

    fun onClick(view: View) {
        val myIntent = Intent(this, ChangeCityController::class.java)
        // Using startActivityForResult since we just get back the city name.
        // Providing an arbitrary request code to check against later.
        startActivityForResult(myIntent, NEW_CITY_CODE)
    }

    protected override fun onResume() {
        super.onResume()
        Log.d(LOGCAT_TAG, "Activity resuming")
        if (useLocation)
            getWeatherForCurrentLocation()
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(LOGCAT_TAG, "onActivityResult() called")

        if (requestCode == NEW_CITY_CODE) {
            if (resultCode == RESULT_OK) {
                var city: String? = null
                if (data != null) {
                    city = data.getStringExtra("City")
                }
                Log.d(LOGCAT_TAG, "New city is " + city!!)

                useLocation = false
                getWeatherForNewCity(city)
            }
        }
    }

    private fun getWeatherForNewCity(city: String?) {
        Log.d(LOGCAT_TAG, "Getting weather for the new city")
        val params = RequestParams()
        params.put("q", city)
        params.put("appid", APP_ID)
        checkNetworkStatus(params)
    }

    private fun getWeatherForCurrentLocation() {
        Log.d(LOGCAT_TAG, "Getting weather for current locationLabel")
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {

                Log.d(LOGCAT_TAG, "onLocationChanged() callback received")
                val longitude = location.longitude.toString()
                val latitude = location.latitude.toString()

                Log.d(LOGCAT_TAG, "longitude is: $longitude")
                Log.d(LOGCAT_TAG, "latitude is: $latitude")

                // Providing 'lat' and 'lon' parameter values
                val params = RequestParams()
                params.put("lat", latitude)
                params.put("lon", longitude)
                params.put("appid", APP_ID)
                checkNetworkStatus(params)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                // Log statements to help you debug your app.
                Log.d(LOGCAT_TAG, "onStatusChanged() callback received. Status: $status")
                Log.d(
                    LOGCAT_TAG,
                    "2 means AVAILABLE, 1: TEMPORARILY_UNAVAILABLE, 0: OUT_OF_SERVICE"
                )
            }

            override fun onProviderEnabled(provider: String) {
                Log.d(LOGCAT_TAG, "onProviderEnabled() callback received. Provider: $provider")
            }

            override fun onProviderDisabled(provider: String) {
                Log.d(LOGCAT_TAG, "onProviderDisabled() callback received. Provider: $provider")
            }
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE
            )
        }

        // Some additional log statements to help you debug
        Log.d(
            LOGCAT_TAG,
            "Location Provider used: " + locationManager!!.getProvider(LOCATION_PROVIDER)!!.name
        )
        Log.d(
            LOGCAT_TAG,
            "Location Provider is enabled: " + locationManager!!.isProviderEnabled(LOCATION_PROVIDER)
        )
        Log.d(
            LOGCAT_TAG,
            "Last known locationLabel (if any): " + locationManager!!.getLastKnownLocation(
                LOCATION_PROVIDER
            )!!
        )
        Log.d(LOGCAT_TAG, "Requesting locationLabel updates")

        locationManager!!.requestLocationUpdates(
            LOCATION_PROVIDER,
            MIN_TIME,
            MIN_DISTANCE,
            locationListener
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(LOGCAT_TAG, "onRequestPermissionsResult(): Permission granted!")

                // Getting weather only if we were granted permission.
                getWeatherForCurrentLocation()
            } else {
                Log.d(LOGCAT_TAG, "Permission denied =( ")
            }
        }
    }

    private fun checkNetworkStatus(params: RequestParams) {
        val client = AsyncHttpClient()

        client.get(WEATHER_URL, params, object : JsonHttpResponseHandler() {
            fun onSuccess(response: JSONObject) {
                Log.d(LOGCAT_TAG, "Success! JSON: $response")
                val weatherData = WeatherDataModel.fromJson(response)
                if (weatherData != null) {
                    updateUI(weatherData)
                }
            }

            fun onFailure(
                statusCode: Int,
                e: Throwable,
                response: JSONObject
            ) {
                Log.e(LOGCAT_TAG, "Fail $e")
                Toast.makeText(this@WeatherController, "Request Failed", Toast.LENGTH_SHORT).show()

                Log.d(LOGCAT_TAG, "Status code $statusCode")
                Log.d(LOGCAT_TAG, "Here's what we got instead $response")
            }
        })
    }

    private fun updateUI(weather: WeatherDataModel) {
        temperatureLabel.text = weather.temperature
        locationLabel.text = weather.city

        val resourceID =
            resources.getIdentifier(weather.iconName, "drawable", packageName)
        weatherImage.setImageResource(resourceID)
    }

    override fun onPause() {
        super.onPause()
        if (locationManager != null)
            locationManager!!.removeUpdates(locationListener)
    }
}