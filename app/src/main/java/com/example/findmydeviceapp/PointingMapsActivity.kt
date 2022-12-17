package com.example.findmydeviceapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.findmydeviceapp.databinding.ActivityPointingMapsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class PointingMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityPointingMapsBinding

    var databaseReference:DatabaseReference?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPointingMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle:Bundle = intent.extras!!
        val phoneNumber = bundle.getString("phoneNumber")

        databaseReference = Firebase.database.reference

        databaseReference!!.child("Users").child(phoneNumber!!).child("location")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    try{
                        val td = snapshot.value as HashMap<String,Any>
                        val latitude = td["latitude"].toString()
                        val longitude = td["longitude"].toString()
                        val lastOnline = td["last online"].toString()
                        sydney = LatLng(latitude.toDouble(),longitude.toDouble())
                        PointingMapsActivity.lastOnline = lastOnline
                        loadMap()
                    }catch (_ :Exception){}
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    fun loadMap(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    companion object{
        var sydney = LatLng(-34.0, 151.0)
        var lastOnline = "not defined"
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        mMap.addMarker(MarkerOptions().position(sydney).title("Last Online: "+lastOnline))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,13f))
    }
}