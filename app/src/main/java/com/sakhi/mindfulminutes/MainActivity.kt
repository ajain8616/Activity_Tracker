package com.sakhi.mindfulminutes

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var currentFragment: Fragment
    private lateinit var userEmailTextView: TextView

    // Firebase Authentication instance
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        val headerView = navigationView.getHeaderView(0)
        userEmailTextView = headerView.findViewById(R.id.userEmailTextView)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_active -> {
                    if (auth.currentUser != null) {
                        replaceFragment(ActiveActivitiesFragment())
                    } else {
                        checkLoginState()
                    }
                    true
                }
                R.id.nav_details -> {
                    if (auth.currentUser != null) {
                        replaceFragment(ActivitiesDetailsFragment())
                    } else {
                        checkLoginState()
                    }
                    true
                }
                R.id.nav_analysis -> {
                    if (auth.currentUser != null) {
                        replaceFragment(PieChartFragment())
                    } else {
                        checkLoginState()
                    }
                    true
                }
                R.id.nav_filtered -> {
                    if (auth.currentUser != null) {
                        replaceFragment(FilteredActivitiesFragment())
                    } else {
                        checkLoginState()
                    }
                    true
                }
                R.id.nav_login -> {
                    if (auth.currentUser == null) {
                        replaceFragment(LoginFragment())
                    }
                    true
                }
                R.id.nav_signUp -> {
                    if (auth.currentUser == null) {
                        replaceFragment(SignUpFragment())
                    }
                    true
                }
                R.id.nav_logout -> {
                    // Handle logout action here
                    logoutUser()
                    true
                }
                else -> false
            }
        }

        // Check if user is logged in and display appropriate fragment
        val isLoggedIn = checkIfUserIsLoggedIn()
        if (savedInstanceState == null) {
            if (isLoggedIn) {
                replaceFragment(ActiveActivitiesFragment(), addToBackStack = false)
                navigationView.setCheckedItem(R.id.nav_active)
            } else {
                checkLoginState()
            }
        }
        showToastBasedOnLoginStatus(isLoggedIn)

        // Display user's name and email if logged in
        if (isLoggedIn) {
            val currentUser = auth.currentUser
            currentUser?.let { updateUserProfileUI(it) }
        }
    }

    private fun showToastBasedOnLoginStatus(isLoggedIn: Boolean) {
        val message = if (isLoggedIn) "User is logged in" else "User is logged out"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkIfUserIsLoggedIn(): Boolean {
        // Check if the user is currently authenticated
        return auth.currentUser != null
    }

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        currentFragment = fragment
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun logoutUser() {
        // Sign out the user from Firebase
        auth.signOut()
        // After signing out, navigate to LoginFragment
        replaceFragment(LoginFragment(), addToBackStack = false)
        navigationView.setCheckedItem(R.id.nav_login)
    }

    private fun updateUserProfileUI(user: FirebaseUser) {
        userEmailTextView.text = user.email
    }

    private fun checkLoginState() {
        // If user is not logged in, navigate to SignUpFragment
        if (auth.currentUser == null) {
            replaceFragment(SignUpFragment(), addToBackStack = false)
            navigationView.setCheckedItem(R.id.nav_signUp)
        } else if (currentFragment is ActiveActivitiesFragment) {
            // If the current fragment is ActiveActivitiesFragment and user email is not registered,
            // navigate to SignUpFragment
            val currentUser = auth.currentUser
            if (currentUser != null && currentUser.email.isNullOrEmpty()) {
                replaceFragment(SignUpFragment(), addToBackStack = false)
                navigationView.setCheckedItem(R.id.nav_signUp)
            }
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
