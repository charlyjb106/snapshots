package com.example.snapshots

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.snapshots.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 21

    private lateinit var mBinding: ActivityMainBinding

    private lateinit var mActiveFragment: Fragment
    private lateinit var mFragmentManager: FragmentManager

    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener
    private var mFirebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setupAuth()
        setupBottomNav()

    }

    /**
     * Setup the firebase auth configuration
     */
    private fun setupAuth() {

        mFirebaseAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener {
            val user = it.currentUser
            Log.i("USER-----> " , user.toString())

            if(user == null) {
                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                    //.setIsSmartLockEnabled(false)   ask for using a logged account
                    .setAvailableProviders(
                        listOf(
                            AuthUI.IdpConfig.EmailBuilder().build(),
                        AuthUI.IdpConfig.GoogleBuilder().build())
                    )
                    .build(), RC_SIGN_IN
                )
            }
        }

    }


    /**
     * Setup the bottomBar configuration
     */
    private fun setupBottomNav() {

        mFragmentManager = supportFragmentManager

        val homeFragment = HomeFragment()
        val addFragment = AddFragment()
        val profileFragment = ProfileFragment()

        mActiveFragment = homeFragment

        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment,profileFragment, ProfileFragment::class.java.name)
            .hide(profileFragment)
            .commit()

        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment,addFragment, AddFragment::class.java.name)
            .hide(addFragment)
            .commit()

        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment,homeFragment, HomeFragment::class.java.name)
            .commit()

        mBinding.bottomNav.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.action_home -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(homeFragment)
                        .commit()
                    mActiveFragment = homeFragment
                    true
                }

                R.id.action_add -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(addFragment)
                        .commit()
                    mActiveFragment = addFragment
                    true
                }

                R.id.action_profile -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(profileFragment)
                        .commit()
                    mActiveFragment = profileFragment
                    true
                }

                else -> false
            }
        }

    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth?.addAuthStateListener(mAuthListener)
    }

    override fun onPause() {
        super.onPause()
        mFirebaseAuth?.removeAuthStateListener(mAuthListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Welcome..." , Toast.LENGTH_SHORT).show()
            } else {
                //user cancel firebase auth
                if(IdpResponse.fromResultIntent(data) == null) {
                    finish()
                }
            }
        }
    }
}

































