package com.raihan.castfit.data.datasource.physicalactivity

import com.raihan.castfit.R
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
                maxAge = 64,
                physicalImg = R.drawable.img_jogging
            ),
            PhysicalActivity(
                name = "Walking",
                type = "Outdoor",
                minAge = 6,
                maxAge = 90,
                physicalImg = R.drawable.img_walking
            ),
            PhysicalActivity(
                name = "Cycling",
                type = "Outdoor",
                minAge = 6,
                maxAge = 90,
                physicalImg = R.drawable.img_cycling
            ),
            PhysicalActivity(
                name = "Senam Otak",
                type = "Indoor",
                minAge = 6,
                maxAge = 90,
                physicalImg = R.drawable.img_brain
            ),
            PhysicalActivity(
                name = "Senam Poco-Poco",
                type = "Indoor",
                minAge = 0,
                maxAge = 90,
                physicalImg = R.drawable.img_exercise
            ),
            PhysicalActivity(
                name = "Senam Lansia",
                type = "Indoor",
                minAge = 65,
                maxAge = 90,
                physicalImg = R.drawable.img_elderly
            ),
            PhysicalActivity(
                name = "Yoga",
                type = "Indoor",
                minAge = 10,
                maxAge = 90,
                physicalImg = R.drawable.img_yoga
            ),
            PhysicalActivity(
                name = "Jumping Jack",
                type = "Indoor",
                minAge = 6,
                maxAge = 64,
                physicalImg = R.drawable.img_jumping_jack
            ),
            PhysicalActivity(
                name = "Push Up",
                type = "Indoor",
                minAge = 10,
                maxAge = 64,
                physicalImg = R.drawable.img_push_up
            ),
            PhysicalActivity(
                name = "Sit up",
                type = "Indoor",
                minAge = 10,
                maxAge = 64,
                physicalImg = R.drawable.img_sit_up
            ),
            PhysicalActivity(
                name = "Crunch",
                type = "Indoor",
                minAge = 10,
                maxAge = 64,
                physicalImg = R.drawable.img_crunch_man
            ),
            PhysicalActivity(
                name = "Rusian Twist",
                type = "Indoor",
                minAge = 10,
                maxAge = 64,
                physicalImg = R.drawable.img_russian_twist
            ),
            PhysicalActivity(
                name = "Back Up",
                type = "Indoor",
                minAge = 10,
                maxAge = 64,
                physicalImg = R.drawable.img_back_up
            ),
            PhysicalActivity(
                name = "Squat",
                type = "Indoor",
                minAge = 10,
                maxAge = 64,
                physicalImg = R.drawable.img_squat
            ),
            PhysicalActivity(
                name = "Step Up Down",
                type = "Indoor",
                minAge = 10,
                maxAge = 64,
                physicalImg = R.drawable.img_step_up_down
            ),
            PhysicalActivity(
                name = "Front Lunges",
                type = "Indoor",
                minAge = 10,
                maxAge = 64,
                physicalImg = R.drawable.img_front_lunges
            ),
            PhysicalActivity(
                name = "Wall Seat",
                type = "Indoor",
                minAge = 10,
                maxAge = 64,
                physicalImg = R.drawable.img_wall_sit
            ),
            PhysicalActivity(
                name = "Plank",
                type = "Indoor",
                minAge = 10,
                maxAge = 64,
                physicalImg = R.drawable.img_plank
            ),
        )
    }
}