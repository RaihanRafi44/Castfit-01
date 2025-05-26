package com.raihan.castfit.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.raihan.castfit.data.repository.LocationRepository
import com.raihan.castfit.data.repository.UserRepository
import com.raihan.castfit.data.repository.UserRepositoryImpl
import com.raihan.castfit.data.repository.WeatherRepository
import com.raihan.castfit.data.repository.WeatherRepositoryImpl
import com.raihan.castfit.data.source.firebase.FirebaseService
import com.raihan.castfit.data.source.firebase.FirebaseServiceImpl
import com.raihan.castfit.data.source.network.service.CastFitApiService
import com.raihan.castfit.presentation.home.HomeViewModel
import com.raihan.castfit.presentation.login.LoginViewModel
import com.raihan.castfit.presentation.profile.ProfileViewModel
import com.raihan.castfit.presentation.register.RegisterViewModel
import com.raihan.castfit.presentation.splashscreen.SplashScreenViewModel
import com.raihan.castfit.data.datasource.auth.AuthDataSource
import com.raihan.castfit.data.datasource.auth.FirebaseAuthDataSource
import com.raihan.castfit.data.datasource.historyactivity.HistoryActivityDataSource
import com.raihan.castfit.data.datasource.historyactivity.HistoryActivityDataSourceImpl
import com.raihan.castfit.data.datasource.location.LocationDataSource
import com.raihan.castfit.data.datasource.physicalactivity.PhysicalDataSource
import com.raihan.castfit.data.datasource.physicalactivity.PhysicalDataSourceImpl
import com.raihan.castfit.data.datasource.progressactivity.ProgressActivityDataSource
import com.raihan.castfit.data.datasource.progressactivity.ProgressActivityDataSourceImpl
import com.raihan.castfit.data.datasource.schedule.ScheduleActivityDataSource
import com.raihan.castfit.data.datasource.schedule.ScheduleActivityDataSourceImpl
import com.raihan.castfit.data.datasource.weather.WeatherDataSource
import com.raihan.castfit.data.datasource.weather.WeatherDataSourceImpl
import com.raihan.castfit.data.repository.HistoryActivityRepository
import com.raihan.castfit.data.repository.HistoryActivityRepositoryImpl
import com.raihan.castfit.data.repository.PhysicalActivityRepository
import com.raihan.castfit.data.repository.PhysicalActivityRepositoryImpl
import com.raihan.castfit.data.repository.ProgressActivityRepository
import com.raihan.castfit.data.repository.ProgressActivityRepositoryImpl
import com.raihan.castfit.data.source.local.database.AppDatabase
import com.raihan.castfit.data.source.local.database.dao.HistoryActivityDao
import com.raihan.castfit.data.source.local.database.dao.ProgressActivityDao
import com.raihan.castfit.data.source.local.database.dao.ScheduleActivityDao
import com.raihan.castfit.presentation.activityuser.ActivityViewModel
import com.raihan.castfit.presentation.chartshistory.ChartsHistoryViewModel
import com.raihan.castfit.presentation.forgotpass.ForgotPassViewModel
import com.raihan.castfit.presentation.main.MainViewModel
import com.raihan.castfit.presentation.recommendation.RecommendationViewModel
import com.raihan.castfit.presentation.schedule.ScheduleViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.dsl.module

object AppModules {
    private val networkModule =
        module{
            single<CastFitApiService> { CastFitApiService.invoke() }
        }

    private val firebaseModule =
        module{
            single<FirebaseAuth> { FirebaseAuth.getInstance() }
            single<FirebaseService> { FirebaseServiceImpl(get(), get()) }
            single<FirebaseFirestore> { FirebaseFirestore.getInstance() }
        }

    private val localModule =
        module{
            single { com.google.gson.Gson() }
            single<AppDatabase> { AppDatabase.createInstance(androidContext()) }
            single<ProgressActivityDao> { get<AppDatabase>().progressActivityDao() }
            single<HistoryActivityDao> { get<AppDatabase>().historyActivityDao() }
            single<ScheduleActivityDao> { get<AppDatabase>().scheduleActivityDao() }
        }

    private val dataSource =
        module{
            single<AuthDataSource> { FirebaseAuthDataSource(get()) }
            single<LocationDataSource> { LocationDataSource(get(), get()) }
            single<WeatherDataSource> { WeatherDataSourceImpl(get()) }
            single<PhysicalDataSource>{ PhysicalDataSourceImpl()}
            single<ProgressActivityDataSource> { ProgressActivityDataSourceImpl(get()) }
            single<HistoryActivityDataSource> { HistoryActivityDataSourceImpl(get()) }
            single<ScheduleActivityDataSource> { ScheduleActivityDataSourceImpl(get()) }
        }

    private val repository =
        module{
            single<UserRepository> { UserRepositoryImpl(get()) }
            single<LocationRepository> { LocationRepository(get()) }
            single<WeatherRepository> { WeatherRepositoryImpl(get()) }
            single<PhysicalActivityRepository> { PhysicalActivityRepositoryImpl(get()) }
            single<ProgressActivityRepository> { ProgressActivityRepositoryImpl(get(), get()) }
            single<HistoryActivityRepository> { HistoryActivityRepositoryImpl(get(), get()) }
        }

    private val viewModel =
        module{
            viewModelOf(::SplashScreenViewModel)
            viewModelOf(::LoginViewModel)
            viewModelOf(::RegisterViewModel)
            viewModelOf(::HomeViewModel)
            viewModelOf(::ForgotPassViewModel)
            viewModelOf(::ProfileViewModel)
            viewModelOf(::MainViewModel)
            viewModelOf(::RecommendationViewModel)
            viewModelOf(::ActivityViewModel)
            viewModelOf(::ChartsHistoryViewModel)
            viewModelOf(::ScheduleViewModel)

        }

    val modules =
        listOf<Module>(
            networkModule,
            localModule,
            dataSource,
            repository,
            viewModel,
            firebaseModule,
        )

}