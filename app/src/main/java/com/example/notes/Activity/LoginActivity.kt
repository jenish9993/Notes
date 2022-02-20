package com.example.notes.Activity


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.notes.R
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class LoginActivity : AppCompatActivity() {

    lateinit var btn_login_register:Button
    var AUTHUI_REQUEST_CODE = 111
    var TAG= "LoginActivity"
    lateinit var mAdView:AdView
    var mInterstitialAd: InterstitialAd? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        MobileAds.initialize(this){}

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)


        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError?.message)
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })

        btn_login_register = findViewById(R.id.btn_login_register)

        if(FirebaseAuth.getInstance().currentUser != null){
            var intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()

        btn_login_register.setOnClickListener {
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this)
            } else {
                Log.d("TAG", "The interstitial ad wasn't ready yet.")
            }
            performLoginAndRegister()
        }
    }

    private fun performLoginAndRegister() {

        var providerList:ArrayList<AuthUI.IdpConfig> = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        var intent:Intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providerList)
            .setLogo(R.mipmap.ic_launcher_round)
            .build()

        startActivityForResult(intent,AUTHUI_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == AUTHUI_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
                Log.d(TAG,"onActivityResult: "+ user!!.uid)

                if (user.metadata!!.creationTimestamp == user.metadata!!.lastSignInTimestamp){
                    Toast.makeText(this,"Welcome New user",Toast.LENGTH_LONG).show()
                }
                else{
                    Toast.makeText(this,"Welcome Back user",Toast.LENGTH_LONG).show()
                }

                val intent=Intent(this, MainActivity::class.java)
                startActivity(intent)
                this.finish()
            }else{
                //Signing Failed

                var response:IdpResponse = IdpResponse.fromResultIntent(data)!!

                if(response == null){
                    Log.d(TAG,"onActivityResult : The user Cancellled the sign in request")
                }else{
                    Log.d(TAG,"onActivityResult: "+response.error)
                }
            }
        }
    }
}