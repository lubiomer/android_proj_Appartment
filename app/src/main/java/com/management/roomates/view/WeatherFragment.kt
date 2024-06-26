package com.management.roomates.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.management.roomates.R
import com.management.roomates.data.model.WeatherResponse
import com.management.roomates.repository.WeatherService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherFragment : Fragment() {

    private lateinit var temperatureTextView: TextView
    private lateinit var windSpeedTextView: TextView
    private lateinit var gpsTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var weatherService: WeatherService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_weather, container, false)
        temperatureTextView = view.findViewById(R.id.temperatureTextView)
        windSpeedTextView = view.findViewById(R.id.windSpeedTextView)
        gpsTextView = view.findViewById(R.id.gpsTextView)
        progressBar = view.findViewById(R.id.progressBar)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        weatherService = retrofit.create(WeatherService::class.java)

        requestLocation()

        return view
    }

    private fun requestLocation() {
        progressBar.visibility = View.VISIBLE


        fusedLocationClient.lastLocation
                .addOnSuccessListener(requireActivity(), OnSuccessListener { location ->
                    fetchWeatherData(32.109333, 34.855499)
                    gpsTextView.text = String.format("GPS Coordinates: 32.109333, 34.855499")
                })
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        weatherService.getCurrentWeather(latitude, longitude, "temperature_2m,wind_speed_10m", "temperature_2m,relative_humidity_2m,wind_speed_10m")
                .enqueue(object : Callback<WeatherResponse> {
                    override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                        if (response.isSuccessful && response.body() != null) {
                            val weather = response.body()!!
                            temperatureTextView.text = "Temperature: ${weather.current.temperature_2m}°C"
                            windSpeedTextView.text = "Wind Speed: ${weather.current.wind_speed_10m}km/h"
                        }
                        progressBar.visibility = View.GONE
                    }

                    override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                        // Error handling here
                        progressBar.visibility = View.GONE
                    }
                })
    }
}
