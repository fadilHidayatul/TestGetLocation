package com.example.testgetlocationuser

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.testgetlocationuser.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.UiSettings
import com.google.android.gms.maps.model.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fused : FusedLocationProviderClient
    private var context : Context? = null
    private lateinit var locationManager : LocationManager

    private var gpsStatus : Boolean = false
    private val REQ_CODE = 1000
    private var lat : Double = 0.0
    private var long : Double = 0.0

    lateinit var gMaps : GoogleMap
    lateinit var center : LatLng
    lateinit var cameraPos : CameraPosition
    var markerOption = MarkerOptions()
    lateinit var latlong : LatLng



    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = this
        fused = LocationServices.getFusedLocationProviderClient(context!!)

        checkStatusGPS(savedInstanceState)

        binding.btGetLocation.setOnClickListener{
//            getCurrentLocation()
//            initMapView(savedInstanceState)
        }
        binding.btOpenMap.setOnClickListener {
            openMap()
        }

//        initMapView(savedInstanceState)


    }

    private fun initMapView(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.onResume()
        binding.mapView.getMapAsync(this)
    }


    @SuppressLint("SetTextI18n")
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQ_CODE
            )
            return
        }

        fused.lastLocation
            .addOnSuccessListener { location ->

                if (location != null){
                    binding.tvLatitude.text = "Latitude : ${location.latitude}"
                    binding.tvLongitude.text = "Longitude : ${location.longitude}"
                    binding.tvProvider.text = "Provider : ${location.provider}"

                    lat = location.latitude
                    long = location.longitude
                }else{
//                    Toast.makeText(applicationContext, "location null", Toast.LENGTH_SHORT).show()
                    refresh()
                }

            }
            .addOnFailureListener{
                Toast.makeText(applicationContext, "Failed Getting Location", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onGPS() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Enable GPS")
            .setPositiveButton("Yes") { dialog: DialogInterface, which ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("No"){ dialog: DialogInterface, which->
                dialog.cancel()
            }

        val alertDialog : AlertDialog = builder.create()
        alertDialog.show()

    }
    private fun checkStatusGPS(savedInstanceState: Bundle?) {
        locationManager = context!!.getSystemService(LOCATION_SERVICE) as LocationManager
        assert(locationManager != null)
        gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (gpsStatus === true) {
            binding.status.text = "GPS Is Enabled"
            getCurrentLocation()
            initMapView(savedInstanceState)
        } else {
            binding.status.text = "GPS Is Disabled"
            onGPS()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            REQ_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(applicationContext, "Grant Permission", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(applicationContext, "Denied Permission", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun openMap(){
        val uri = Uri.parse("geo:0,0?q=$lat,$long")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }

    override fun onRestart() {
        super.onRestart()
//        checkStatusGPS(savedInstanceState) //setelah activate GPS
        getCurrentLocation()
    }

    private fun refresh(){
        getCurrentLocation()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        gMaps = googleMap!!

        //setLokasi
        center = LatLng(lat,long)
        cameraPos = CameraPosition.Builder().target(center).zoom(12F).build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos))
        latlong = LatLng(lat,long)

        //UISettingMaps
        gMaps.isIndoorEnabled = true
        var ui : UiSettings = gMaps.uiSettings
        ui.isIndoorLevelPickerEnabled = true
        ui.isCompassEnabled = true
        ui.isCompassEnabled = true

        //titik marker di maps
        var titik : Bitmap = BitmapFactory.decodeResource(resources, R.drawable.common_google_signin_btn_icon_dark)
        var t = Bitmap.createScaledBitmap(titik,50,50,false)

        //setMarker
        markerOption.position(latlong)
        markerOption.icon(BitmapDescriptorFactory.fromBitmap(t))

        //implement marker
        gMaps.addMarker(markerOption)
        gMaps.setOnInfoWindowClickListener { marker : Marker ->
            Toast.makeText(context,"Your Location",Toast.LENGTH_SHORT).show()
        }
    }



}
