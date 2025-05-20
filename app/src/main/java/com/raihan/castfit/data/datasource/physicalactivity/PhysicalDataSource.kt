package com.raihan.castfit.data.datasource.physicalactivity

import com.raihan.castfit.data.model.PhysicalActivity


interface PhysicalDataSource {
    fun getPhysicalActivitiesData(): List<PhysicalActivity>
}

class PhysicalDataSourceImpl : PhysicalDataSource {
    override fun getPhysicalActivitiesData(): List<PhysicalActivity> {
        return mutableListOf(
            PhysicalActivity(
                name = "Jogging",
                type = "Outdoor",
                minAge = 6,
                maxAge = 64
            ),
            PhysicalActivity(
                name = "Walking",
                type = "Outdoor",
                minAge = 6,
                maxAge = 90
            ),
            PhysicalActivity(
                name = "Cycling",
                type = "Outdoor",
                minAge = 6,
                maxAge = 90
            ),
            PhysicalActivity(
                name = "Senam Otak",
                type = "Indoor",
                minAge = 6,
                maxAge = 90
            ),
            PhysicalActivity(
                name = "Senam Poco-Poco",
                type = "Indoor",
                minAge = 0,
                maxAge = 90
            ),
            PhysicalActivity(
                name = "Senam Lansia",
                type = "Indoor",
                minAge = 65,
                maxAge = 90
            ),
            PhysicalActivity(
                name = "Yoga",
                type = "Indoor",
                minAge = 10,
                maxAge = 90
            ),
            PhysicalActivity(
                name = "Jumping Jack",
                type = "Indoor",
                minAge = 6,
                maxAge = 64
            ),
            PhysicalActivity(
                name = "Push Up",
                type = "Indoor",
                minAge = 10,
                maxAge = 64
            ),
            PhysicalActivity(
                name = "Sit up",
                type = "Indoor",
                minAge = 10,
                maxAge = 64
            ),
            PhysicalActivity(
                name = "Crunch",
                type = "Indoor",
                minAge = 10,
                maxAge = 64
            ),
            PhysicalActivity(
                name = "Rusian Twist",
                type = "Indoor",
                minAge = 10,
                maxAge = 64
            ),
            PhysicalActivity(
                name = "Back Up",
                type = "Indoor",
                minAge = 10,
                maxAge = 64
            ),
            PhysicalActivity(
                name = "Squat",
                type = "Indoor",
                minAge = 10,
                maxAge = 64
            ),
            PhysicalActivity(
                name = "Step Up Down",
                type = "Indoor",
                minAge = 10,
                maxAge = 64
            ),
            PhysicalActivity(
                name = "Front Lunges",
                type = "Indoor",
                minAge = 10,
                maxAge = 64
            ),
            PhysicalActivity(
                name = "Wall Seat",
                type = "Indoor",
                minAge = 10,
                maxAge = 64
            ),
            PhysicalActivity(
                name = "Plank",
                type = "Indoor",
                minAge = 10,
                maxAge = 64
            ),
        )
    }
}