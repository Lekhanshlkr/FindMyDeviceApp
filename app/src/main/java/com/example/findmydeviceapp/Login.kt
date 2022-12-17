package com.example.findmydeviceapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login2.*
import java.text.SimpleDateFormat
import java.util.*

class Login : AppCompatActivity() {

    var mAuth:FirebaseAuth?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        mAuth = Firebase.auth
        signInAnonymously()
    }

    fun signInAnonymously(){
        mAuth!!.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(applicationContext, "Firebase Authentication Successful.", Toast.LENGTH_SHORT).show()
                    val user = mAuth!!.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(applicationContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun buRegisterEvent(view: View){

        val userData = UserData(this)
        userData.savePhone(etPhoneNumber.text.toString())

        //get datetime
        val df = SimpleDateFormat("yyyy/MM/dd HH:MM:ss")
        val date = Date()

        //save to firebase database
        var mDatabase: DatabaseReference = Firebase.database.reference
        mDatabase.child("Users").child(etPhoneNumber.text.toString()).child("request").setValue(df.format(date).toString())
        mDatabase.child("Users").child(etPhoneNumber.text.toString()).child("finders").setValue(df.format(date).toString())

        //end activity
        finish()
    }
}