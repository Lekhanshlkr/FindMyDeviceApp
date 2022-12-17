package com.example.findmydeviceapp

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class MyService:Service() {

    var databaseReference:DatabaseReference?=null

    override fun onBind(intent: Intent?): IBinder? {

        return null
    }

    override fun onCreate() {
        super.onCreate()
        databaseReference = Firebase.database.reference
        isServiceRunning=true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        //code run in background for long time

        var location = MyLocationListener()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3,3f,location)

        //listen to request
        var userData = UserData(this)
        val myPhoneNumber = userData!!.loadPhoneNumber()
        databaseReference!!.child("Users").child(myPhoneNumber).child("request").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(myLocation==null) return

                    databaseReference!!.child("Users").child(myPhoneNumber)
                        .child("location").child("latitude").setValue(myLocation!!.latitude)
                    databaseReference!!.child("Users").child(myPhoneNumber)
                        .child("location").child("longitude").setValue(myLocation!!.longitude)

                    val df = SimpleDateFormat("yyyy/MM/dd HH:MM:ss")
                    val date = Date()
                    databaseReference!!.child("Users").child(myPhoneNumber)
                        .child("location").child("last online").setValue(df.format(date).toString())


                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            }
        )

        return Service.START_NOT_STICKY
    }

    companion object{
        var myLocation: Location?=null
        var isServiceRunning = false
    }

    inner class MyLocationListener: LocationListener {

        constructor():super(){
            myLocation = Location("me")
            myLocation!!.longitude=0.0
            myLocation!!.latitude=0.0
        }

        override fun onLocationChanged(p0: Location) {
            myLocation=p0
        }
    }


}