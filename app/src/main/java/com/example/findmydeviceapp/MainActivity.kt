package com.example.findmydeviceapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.contact_ticket.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    var listOfContact = ArrayList<UserContact>()
    var adapter: MyContactAdapter?=null
    var userData:UserData?=null

    var databaseReference:DatabaseReference?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userData = UserData(applicationContext)
        userData!!.isFirstTimeLoad()

        databaseReference = Firebase.database.reference

        //for debug only
//        dummyData()

        adapter = MyContactAdapter(this,listOfContact)
        lvShowContactMain.adapter = adapter

        lvShowContactMain.onItemClickListener  = AdapterView.OnItemClickListener{
            parent,view,position,id ->
            val userInfo = listOfContact[position]

            val df = SimpleDateFormat("yyyy/MM/dd HH:MM:ss")
            val date = Date()
            //save to database
            databaseReference!!.child("Users").child(userInfo.userPhone!!).child("request").setValue(df.format(date).toString())

            val intent = Intent(applicationContext,PointingMapsActivity::class.java)
            intent.putExtra("phoneNumber",userInfo.userPhone)
            startActivity(intent)

        }


    }


    override fun onResume() {
        super.onResume()

        if(userData!!.loadPhoneNumber()=="empty"){
            return
        }
        refreshUsers()

        if (MyService.isServiceRunning) return
        checkContactPermission()
        checkLocationPermission()
    }

    fun refreshUsers(){



        databaseReference!!.child("Users").child(userData!!.loadPhoneNumber()).child("finders").addValueEventListener(object:
        ValueEventListener{

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                listOfContact.clear()

                if(dataSnapshot.value is String || dataSnapshot.value==null){
                    listOfContact.add(UserContact("No_Users","Null Data"))
                    adapter!!.notifyDataSetChanged()
                    return
                }
                val td:HashMap<String, Any> = dataSnapshot.value as HashMap<String, Any>

                for(key in td.keys){
                    var name = mapOfContacts.get(key).toString()
                    listOfContact.add(UserContact(name,key))
                }
                adapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    //for debugging
    fun dummyData(){
        listOfContact.add(UserContact("Lekhansh","9005267771"))
        listOfContact.add(UserContact("Kanak","9540887989"))
        listOfContact.add(UserContact("Jagriti","6399664226"))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.addTracker ->{
                val intent = Intent(this,MyTrackers::class.java)
                startActivity(intent)
            }
            R.id.help ->{
                //TODO:: ask for help from friend
            }
            else ->{
                return super.onOptionsItemSelected(item)
            }
        }

        return true
    }

    //for permissionchecks
    val CONTACT_CODE = 123
    fun checkContactPermission(){

        if(Build.VERSION.SDK_INT>=29){
            if(ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED){

                requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS),CONTACT_CODE)
            }
        }
        loadContact()
    }

    val LOCATION_CODE = 124
    fun checkLocationPermission(){

        if(Build.VERSION.SDK_INT>=29){
            if(ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){

                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),LOCATION_CODE)
            }
        }
        getUserLocation()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode){
            CONTACT_CODE ->{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    loadContact()
                }else{
                    Toast.makeText(this,"Cannot Access Contact", Toast.LENGTH_LONG).show()
                }
            }
            LOCATION_CODE ->{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getUserLocation()
                }else{
                    Toast.makeText(this,"Cannot Access Location", Toast.LENGTH_LONG).show()
                }
            }else ->{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        }
    }

    var mapOfContacts = HashMap<String,String>()
    @SuppressLint("Range")
    fun loadContact(){

        mapOfContacts.clear()
        val cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null)
        cursor!!.moveToFirst()
        do{
            val name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            mapOfContacts.put(UserData.formatNumber(phoneNumber),name)
        } while(cursor!!.moveToNext())
    }

    fun getUserLocation(){

        //start service
        if(!MyService.isServiceRunning){
            val intent = Intent(baseContext,MyService::class.java)
            startService(intent)
        }
    }

    //adapter for displaying contacts
    inner class MyContactAdapter: BaseAdapter {
        var contactList = ArrayList<UserContact>()
        var context: Context?=null
        constructor(context: Context, contactList:ArrayList<UserContact>){
            this.contactList=contactList
            this.context=context
        }

        override fun getCount(): Int {
            return contactList.size
        }

        override fun getItem(p0: Int): Any {
            return contactList[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        @SuppressLint("ViewHolder")
        override fun getView(index: Int, p1: View?, p2: ViewGroup?): View {

            val curContact = contactList[index]

            if(curContact.userName=="No_Users"){
                val myView = layoutInflater.inflate(R.layout.no_user_ticket,null)
                return myView
            }
            else{
                val myView = layoutInflater.inflate(R.layout.contact_ticket,null)

                myView.tvName.text = curContact.userName
                myView.tvPhoneNumber.text = curContact.userPhone

                return myView
            }
        }

    }

}