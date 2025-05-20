package com.raihan.castfit.data.repository

import com.raihan.castfit.data.datasource.weather.WeatherDataSource
import com.raihan.castfit.data.mapper.toWeather
import com.raihan.castfit.data.model.Weather
import com.raihan.castfit.utils.ResultWrapper
import com.raihan.castfit.utils.proceedFlow
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

    fun getWeathers(
        query: String
    ) : Flow<ResultWrapper<Weather>>
}

class WeatherRepositoryImpl(private val dataSource: WeatherDataSource) : WeatherRepository {
    override fun getWeathers(query: String): Flow<ResultWrapper<Weather>> {
        return proceedFlow {
            val response = dataSource.getWeatherData(query)
            response.toWeather()
        }
    }
}

