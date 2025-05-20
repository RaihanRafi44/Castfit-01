package com.raihan.castfit.data.repository

import com.raihan.castfit.data.datasource.physicalactivity.PhysicalDataSource
import com.raihan.castfit.data.model.PhysicalActivity


interface PhysicalActivityRepository {
    fun getAllActivities(): List<PhysicalActivity>
    fun getIndoorActivities(): List<PhysicalActivity>
    fun getOutdoorActivities(): List<PhysicalActivity>
}

class PhysicalActivityRepositoryImpl(
    private val dataSource: PhysicalDataSource
) : PhysicalActivityRepository {

    override fun getAllActivities(): List<PhysicalActivity> = dataSource.getPhysicalActivitiesData()

    override fun getIndoorActivities(): List<PhysicalActivity> =
        dataSource.getPhysicalActivitiesData().filter { it.type == "Indoor" }

    override fun getOutdoorActivities(): List<PhysicalActivity> =
        dataSource.getPhysicalActivitiesData().filter { it.type == "Outdoor" }
}