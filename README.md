# Castfit

## Overview

**Castfit** is a physical activity recommendation app based on weather conditions, device location, and user age. The app allows users to select one activity at a time, track progress, and maintain a structured activity history.

## Features

- **Homepage**: Allows users to check the username, location, and weather data. On this page there is also a menu for chart history and creating activity schedules.
- **Sign In**: Users can log in to their accounts.
- **Sign Up**: New users can create an account.
- **Reset Password**: Users can reset their password if they forget it.
- **Recommendation Physical Activities**: Allows users to select one of the recommended physical activities that have been tailored to the device's location, weather conditions, and the user's age.
- **Activity On Progress Monitoring**: Users can monitor the progress of previously selected activities.
- **Activity History**: Displays a history of physical activities that have been completed by the user.
- **Chart History**: Displays a history of physical activities completed by the user using a bar graph accessed via the homepage.
- **Create Schedule**: Users can create activity schedules filled with the schedule date and name of the physical activity.
- **Profile**: Users can update their profile data.
- **Schedule Notification**: Users receive schedule notifications when the scheduled date arrives.

## Tech Stack

- Using [WeatherAPI](https://www.weatherapi.com/) for weather data
- Using Firebase for authentication
- Using Geocoder to convert GPS coordinates into human-readable addresses
- Using Material3 as the design system to build a modern and consistent user interface.
- Using Koin for dependency injection
- Using MVVM (Model, View, View Model) for design pattern

## Installation

To get started with Castfit, follow these steps:

1. Clone the repository:
    ```bash
    git clone https://github.com/your_username/Castfit-01.git
    ```
2. Open the project in Android Studio.
3. Build and run the application on your Android device or emulator.

## Requirements

- Android Studio Koala++
- Android Smartphone minimum version is 8.0 (Oreo)
- Internet connection

## Usage

1. Launch the application.
2. Sign in or create a new account.
3. Allow use of location from the system to display weather data.
4. Fill in the date of birth on the profile page.
5. Select one of the recommended physical activities via the home page.
6. Once selected, the physical activity will enter the on progress status on the activity page and you can see the time count of the activity.
7. If you have finished the activity, then you confirm completion and the physical activity will move to the history menu.

## Screenshots

![App Screenshot](https://github.com/RaihanRafi44/ImageSource/blob/cd8518c8cc2e554c6a62552727de9402f7dcaa63/Preview%20Castfit/Preview%20All%20Castfit.png?raw=true)