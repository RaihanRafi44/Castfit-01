package com.raihan.castfit.data.source.network.service

import com.raihan.castfit.BuildConfig
import com.raihan.castfit.BuildConfig.API_KEY
import com.raihan.castfit.data.source.network.model.weather.WeatherData
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface CastFitApiService {

    @GET("forecast.json")
    suspend fun getWeatherData(
        @Query("key") key: String = API_KEY,
        @Query("q") query: String
    ): WeatherData

    companion object {
        @JvmStatic
        operator fun invoke(): CastFitApiService {
            val okHttpClient =
                OkHttpClient.Builder()
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .build()
            val retrofit =
                Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()
            return retrofit.create(CastFitApiService::class.java)
        }
    }
}