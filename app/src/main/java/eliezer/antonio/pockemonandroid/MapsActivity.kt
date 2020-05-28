package eliezer.antonio.pockemonandroid

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var ACCESSLOCATION = 123
    private lateinit var mMap: GoogleMap
    private var listPockemons = ArrayList<Pockemon>()
    private var location: Location? = null
    private var playerPower = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        checkPermmison()
        loadPockemon()

    }

    private fun checkPermmison() {
        if (Build.VERSION.SDK_INT > 23) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    ACCESSLOCATION
                )
                return

            }
        }
        getUserLocation()
    }

    //get user location
    private fun getUserLocation() {
        Toast.makeText(this, "User location acees on", Toast.LENGTH_SHORT).show();
        //TODO: Will implement later

        var myLocation = MyLocationListener()
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3, 3f, myLocation)

        var myThread = MyThread()
        myThread.start()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            ACCESSLOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getUserLocation()
                } else {
                    Toast.makeText(this, "We cannot access to your location", Toast.LENGTH_SHORT)
                        .show();
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    inner class MyLocationListener : LocationListener {


        constructor() {

            location = Location("Start")
            location!!.longitude = 0.0
            location!!.latitude = 0.0
        }

        override fun onLocationChanged(p0: Location?) {
            location = p0
        }

        /**
         * This callback will never be invoked and providers can be considers as always in the
         * [LocationProvider.AVAILABLE] state.
         *
         */
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            TODO("Not yet implemented")
        }

        /**
         * Called when the provider is enabled by the user.
         *
         * @param provider the name of the location provider associated with this
         * update.
         */
        override fun onProviderEnabled(provider: String?) {
            TODO("Not yet implemented")
        }

        /**
         * Called when the provider is disabled by the user. If requestLocationUpdates
         * is called on an already disabled provider, this method is called
         * immediately.
         *
         * @param provider the name of the location provider associated with this
         * update.
         */
        override fun onProviderDisabled(provider: String?) {
            TODO("Not yet implemented")
        }

    }

    var oldLocation: Location? = null

    inner class MyThread : Thread {
        constructor() : super() {
            oldLocation = Location("Start")
            oldLocation!!.longitude = 0.0
            oldLocation!!.latitude = 0.0

        }

        override fun run() {
            while (true) {
                try {
                    if (oldLocation!!.distanceTo(location) == 0f) {
                        continue
                    }

                    oldLocation = location
                    runOnUiThread {
                        mMap.clear()
                        // show me
                        val sydney = LatLng(location!!.latitude, location!!.longitude)
                        mMap.addMarker(
                            MarkerOptions()
                                .position(sydney)
                                .title("Eliezer")
                                .snippet("Minha localização")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mario))
                        )

                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                sydney, 70f
                            )
                        )
                        //show pockmons

                        for (i in 0..listPockemons.size - 1) {
                            var newPockemon = listPockemons[i]
                            if (newPockemon.isCatch == false) {

                                val pockemonLoc = LatLng(
                                    newPockemon.location!!.latitude,
                                    newPockemon.location!!.longitude
                                )
                                mMap.addMarker(
                                    MarkerOptions()
                                        .position(pockemonLoc)
                                        .title(newPockemon.name)
                                        .snippet(newPockemon.des + "power" + newPockemon.power)
                                        .icon((BitmapDescriptorFactory.fromResource(newPockemon.image!!)))
                                )


                                if (location!!.distanceTo(newPockemon.location) < 2) {
                                    newPockemon.isCatch = true
                                    listPockemons[i] = newPockemon
                                    playerPower += newPockemon.power!!
                                    Toast.makeText(
                                        applicationContext,
                                        "You catch ne packmon yout new is $playerPower",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }


                            }

                        }
                    }
                    Thread.sleep(1000)

                } catch (ex: Exception) {


                }
            }


        }
    }


    fun loadPockemon() {


        listPockemons.add(
            Pockemon(
                R.drawable.charmander,
                "Charmander", "Charmander living in japan", 55.0, -14.921445, 13.498087
            )
        )
        listPockemons.add(
            Pockemon(
                R.drawable.bulbasaur,
                "Bulbasaur", "Bulbasaur living in usa", 90.5, -14.920678, 13.501306
            )
        )
        listPockemons.add(
            Pockemon(
                R.drawable.squirtle,
                "Squirtle", "Squirtle living in iraq", 33.5, 37.7816621152613, -122.41225361824
            )
        )

    }
}

