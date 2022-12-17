package com.example.findmydeviceapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import com.example.findmydeviceapp.UserData.Companion.myTrackers
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login2.*
import kotlinx.android.synthetic.main.activity_my_trackers.*
import kotlinx.android.synthetic.main.contact_ticket.view.*

class MyTrackers : AppCompatActivity() {

    var listOfContact = ArrayList<UserContact>()
    var adapter:MyContactAdapter?=null
    var userData:UserData?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_trackers)

        userData = UserData(applicationContext)
        adapter = MyContactAdapter(this,listOfContact)
        lvShowContact.adapter = adapter

        lvShowContact.onItemClickListener = AdapterView.OnItemClickListener{
                parent,view,position,id ->
            val userInfo = listOfContact[position]
            myTrackers.remove(userInfo.userPhone)
            refreshData()

            //save to shared pref
            userData!!.saveContactInfo()

            val mDatabase:DatabaseReference = Firebase.database.reference
            mDatabase.child("Users").child(userInfo.userPhone!!).child("finders")
                .child(userData!!.loadPhoneNumber()).removeValue()
        }

        userData!!.loadContactInfo()
        refreshData()
    }




    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.tracker_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.addContact ->{
               checkPermission()
            }
            R.id.finishActivity ->{
                finish()
            }
            else ->{
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    val CONTACT_CODE = 123

    fun checkPermission(){

        if(Build.VERSION.SDK_INT>=29){
            if(ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED){

                requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS),CONTACT_CODE)
            }
        }
        pickContact()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode){
            CONTACT_CODE ->{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    pickContact()
                }else{
                    Toast.makeText(this,"Cannot Access Contact",Toast.LENGTH_LONG).show()
                }
            }else ->{
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    val PICK_CODE = 1234
    fun pickContact(){

        val intent = Intent(Intent.ACTION_PICK,ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent,PICK_CODE)
    }

    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){

            PICK_CODE ->{

                if(resultCode == Activity.RESULT_OK){
                    val contactData = data!!.data
                    val cursor = contentResolver.query(contactData!!,null,null,null,null)

                    if(cursor!!.moveToFirst()){

                        val id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                        val hasPhone = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                        if(hasPhone.equals("1")){
                            val phonesCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id,null,null)

                            phonesCursor!!.moveToFirst()
                            var phoneNumber = phonesCursor.getString(phonesCursor.getColumnIndex("data1"))
                            val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                            phoneNumber = UserData.formatNumber(phoneNumber)
                            myTrackers.put(phoneNumber,name)
                            refreshData()
                            // save to shared pref
                            userData!!.saveContactInfo()

                            //save to realtime DB
                            val mDatabase:DatabaseReference = Firebase.database.reference
                            mDatabase.child("Users").child(phoneNumber).child("finders")
                                                                            .child(userData!!.loadPhoneNumber()).setValue(true)

                        }

                    }
                }
            }
            else ->{
                super.onActivityResult(requestCode, resultCode, data)
            }

        }

    }
    fun refreshData(){
        listOfContact.clear()

        for((key,value) in myTrackers){
            listOfContact.add(UserContact(value,key))
        }
        adapter!!.notifyDataSetChanged()
    }

    //adapter for displaying contacts
    inner class MyContactAdapter:BaseAdapter{
        var contactList = ArrayList<UserContact>()
        var context:Context?=null
        constructor(context:Context,contactList:ArrayList<UserContact>){
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
            val myView = layoutInflater.inflate(R.layout.contact_ticket,null)

            myView.tvName.text = curContact.userName
            myView.tvPhoneNumber.text = curContact.userPhone

            return myView
        }

    }
}