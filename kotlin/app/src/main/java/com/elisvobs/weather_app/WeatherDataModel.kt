package com.elisvobs.weather_app

import org.json.JSONException
import org.json.JSONObject

class WeatherDataModel {
    internal var temperature: String? = null
    var city: String? = null
        private set
    var iconName: String? = null
        private set
    private var condition: Int = 0

    fun getTemperature(): String {
        return temperature!! + "°"
    }

    companion object {

        fun fromJson(jsonObject: JSONObject): WeatherDataModel? {

            try {
                val weatherData = WeatherDataModel()
                weatherData.city = jsonObject.getString("name")
                weatherData.condition =
                    jsonObject.getJSONArray("weather").getJSONObject(0).getInt("id")
                weatherData.iconName = updateWeatherIcon(weatherData.condition)

                val tempResult = jsonObject.getJSONObject("main").getDouble("temp") - 273.15
                val roundedValue = Math.rint(tempResult).toInt()

                weatherData.temperature = Integer.toString(roundedValue)
                return weatherData
            } catch (e: JSONException) {
                e.printStackTrace()
                return null
            }

        }

        fun updateWeatherIcon(condition: Int): String {

            if (condition >= 0 && condition < 300) {
                return "tsstorm1"
            } else if (condition >= 300 && condition < 500) {
                return "light_rain"
            } else if (condition >= 500 && condition < 600) {
                return "shower3"
            } else if (condition >= 600 && condition < 700) {
                return "snow4"
            } else if (condition > 700 && condition < 771) {
                return "fog"
            } else if (condition > 772 && condition < 800) {
                return "tstorm3"
            } else if (condition == 800) {
                return "sunny"
            } else if (condition >= 801 && condition < 804) {
                return "cloudy2"
            } else if (condition >= 900 && condition < 902) {
                return "tstorm3"
            } else if (condition == 903) {
                return "snow5"
            } else if (condition == 904) {
                return "sunny"
            } else if (condition >= 905 && condition < 1000) {
                return "tstorm3"
            }

            return "dunno"
        }
    }

}