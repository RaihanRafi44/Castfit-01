package com.raihan.castfit.presentation.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.raihan.castfit.R
import com.raihan.castfit.databinding.ActivityMainBinding
import com.raihan.castfit.presentation.activityuser.ActivityFragment
import com.raihan.castfit.presentation.home.HomeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val homeViewModel: HomeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(binding.root)
        setupBottomNav()
        //enableEdgeToEdge()
        /*window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }*/
    }

    private fun setupBottomNav() {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        binding.navView.setupWithNavController(navController)

    }

    /*fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "SCHEDULE_CHANNEL",
                "Jadwal Aktivitas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi untuk aktivitas terjadwal"
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }*/

    /*
        private fun handleNotificationNavigation() {
            // Check if activity was opened from notification
            val navigateTo = intent.getStringExtra("navigate_to")
            if (navigateTo == "activity_fragment") {
                // Navigate directly to activity fragment
                navigateToActivityFragment()
                // Set bottom navigation selection
                binding.navView.selectedItemId = R.id.nav_host_fragment_activity_main
            }
        }

        private fun navigateToActivityFragment() {
            // Navigate to ActivityFragment
            // Implementasi tergantung pada struktur navigasi aplikasi Anda
            // Contoh menggunakan FragmentTransaction:

            val fragment = ActivityFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_activity_main, fragment)
                .commit()

            // Atau jika menggunakan Navigation Component:
            // findNavController(R.id.nav_host_fragment).navigate(R.id.activityFragment)
        }

        override fun onNewIntent(intent: Intent) {
            super.onNewIntent(intent)
            setIntent(intent)
            // Handle new intent ketika activity sudah ada di background
            handleNotificationNavigation()
        }*/
}