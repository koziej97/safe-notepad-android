package com.lukaszkoziej.safenotepad

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.ui.onNavDestinationSelected
import com.lukaszkoziej.safenotepad.databinding.ActivityMainBinding
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.AppBarLayout

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleEdgeToEdge()

        setSupportActionBar(binding.toolbar)

        //Responsible for the navigation arrow on the AppBar
        navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun handleEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val appBar: AppBarLayout? = binding.root.findViewById(R.id.app_bar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            appBar?.updatePadding(top = sysBars.top)

            val navHost = view.findViewById<View>(R.id.nav_host_fragment_content_main)
            navHost?.updatePadding(bottom = sysBars.bottom)

            insets
        }
    }
}
