package br.com.hugo.victor.mapas

import android.Manifest
import android.location.Address
import android.location.Geocoder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.location.Location
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import java.text.DateFormat
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private val INTERVAL = (1000 * 10).toLong()
    private val FASTEST_INTERVAL = (1000 * 5).toLong()
    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient: GoogleApiClient

    lateinit var mCurrentLocation: Location
    lateinit var mLastUpdateTime: String
    lateinit var mLocationRequest: LocationRequest

    var REQUEST_GPS = 212

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btPesquisar.setOnClickListener {
            mMap.clear()

            if (!etEdnereco.text.isEmpty()) {
                val geocoder = Geocoder(this)
                val address: List<Address>?

                address = geocoder.getFromLocationName(etEdnereco.text.toString(), 1)

                if (address.isNotEmpty()) {
                    val location = address.get(0)
                    adicionarMarcador(location.latitude, location.longitude, getString(R.string.txt_endereco_pesquisado))
                } else {
                    var alert = AlertDialog.Builder(this).create()
                    alert.setTitle(getString(R.string.txt_ops_deu_ruim))
                    alert.setMessage(getString(R.string.txt_end_nao_encontrado))

                    alert.setCancelable(false)
                    alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.txt_ok), { dialogInterface, inteiro ->
                        alert.dismiss()
                    })
                    alert.show()
                }
            } else {
                Toast.makeText(this, getString(R.string.txt_endereco_branco), Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onLocationChanged(p0: Location?) {
        mCurrentLocation = p0!!
        mLastUpdateTime = DateFormat.getTimeInstance().format(Date())
        adicionarMarcador(mCurrentLocation.latitude, mCurrentLocation.longitude, getString(R.string.txt_endereco_pesquisado))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_GPS -> {
                if (grantResults.size == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("TAG", "Permissão negada pelo usuário")
                } else {
                    Log.i("TAG", "Permissao concedida pelo usuario")
                }
                return
            }
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.i("TAG", "SUSPENSO")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.i("TAG", "Erro de conexão")
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        callConnection()
    }

    override fun onConnected(p0: Bundle?) {
        createLocationRequest()
        checkPermission()

        val minhaLocalizacao = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
        if (minhaLocalizacao != null) {
            adicionarMarcador(minhaLocalizacao.latitude, minhaLocalizacao.longitude, getString(R.string.txt_endereco_pesquisado))
        }

    }

    protected fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = INTERVAL
        mLocationRequest.fastestInterval = FASTEST_INTERVAL
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    protected fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_GPS)
    }

    protected fun stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
    }

    private fun checkPermission() {
        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                val builder = AlertDialog.Builder(this)

                builder.setMessage("Necessária a permissao para GPS").setTitle("Permissao Requerida")

                builder.setPositiveButton("OK") { dialog, id ->
                    requestPermission()
                }

                val dialog = builder.create()
                dialog.show()

            } else {
                requestPermission()
            }
        }
    }

    @Synchronized
    fun callConnection() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build()

        mGoogleApiClient.connect()
    }

    fun adicionarMarcador(latitude: Double, longitude: Double, titulo: String) {
        val market = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions()
                .position(market)
                .title(titulo))
        // Para adicionar uma imagem no ponto: addMarker(icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_round))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(market, 16f))
    }

}
