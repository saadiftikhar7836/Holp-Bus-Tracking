package com.codesses.holp.common.views.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.codesses.holp.R
import com.codesses.holp.common.utils.SharedStorage
import com.codesses.holp.databinding.ActivityStartupBinding
import com.codesses.holp.service.MyLocationService

class StartupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartupBinding

    private lateinit var navigationController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.mainHostFragment) as NavHostFragment

        val navController = navHostFragment.findNavController()

        if (SharedStorage.isTripStarted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(
                    Intent(this, MyLocationService::class.java)
                )
            } else {
                startService(
                    Intent(this, MyLocationService::class.java)
                )
            }
            navController.navigate(R.id.action_choose_role_fragment_to_driver_main_fragment)
            navController.navigate(R.id.action_driver_main_fragment_to_tripFragment)
        }
        navigationController = Navigation.findNavController(this, R.id.mainHostFragment)

    }

    override fun onSupportNavigateUp(): Boolean {
        return navigationController.navigateUp() || super.onSupportNavigateUp()
    }
}