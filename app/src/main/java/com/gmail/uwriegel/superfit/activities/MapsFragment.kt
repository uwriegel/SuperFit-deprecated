package com.gmail.uwriegel.superfit.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gmail.uwriegel.superfit.R
import com.gmail.uwriegel.superfit.maps.LocationSetter
import com.gmail.uwriegel.superfit.maps.MapView
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.rotation.RotateView
import org.mapsforge.map.android.util.AndroidPreferences
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.datastore.MapDataStore
import org.mapsforge.map.layer.cache.TileCache
import org.mapsforge.map.model.common.PreferencesFacade
import org.mapsforge.map.reader.MapFile
import org.mapsforge.map.rendertheme.InternalRenderTheme
import java.io.File
import java.util.ArrayList

class MapsFragment : Fragment(), LocationSetter {
    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.map_fragment, container, false)
        this.preferencesFacade = AndroidPreferences(this.activity!!.getSharedPreferences(this.javaClass.simpleName, Context.MODE_PRIVATE))
        mapView = layout.findViewById(R.id.mapView)
        layout.findViewById<RotateView>(R.id.rotateView).setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        mapView!!.model.frameBufferModel.overdrawFactor = 1.0
        mapView!!.model.init(this.preferencesFacade)
        mapView!!.isClickable = true
        mapView!!.mapScaleBar!!.isVisible = true // false
        mapView!!.setBuiltInZoomControls(false) // false
        mapView!!.mapZoomControls.isAutoHide = true
        mapView!!.mapZoomControls.setZoomLevelMin(0.toByte())
        mapView!!.mapZoomControls.setZoomLevelMax(24.toByte())

        createTileCaches()
        createLayers()

        mapView!!.setLocationSetter(this)

        locationManager = activity!!.getSystemService(Activity.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, locationListener)

        return layout
    }

    override fun changeValue(setLocation: Boolean) {
        this.setLocation = setLocation
        if (this.setLocation && recentLocation != null)
            mapView!!.setCenter(LatLong(recentLocation!!.latitude, recentLocation!!.longitude))
    }

    fun setLocationCenter() {
        mapView!!.onCenter()
    }

    override fun onPause() {
        mapView!!.model.save(this.preferencesFacade)
        this.preferencesFacade.save()
        super.onPause()
    }

    override fun onDestroyView() {
        mapView = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun createTileCaches() {
        this.tileCaches.add(AndroidUtil.createTileCache(activity, this.javaClass.simpleName,
                mapView!!.model.displayModel.tileSize, 1.0f,
                mapView!!.model.frameBufferModel.overdrawFactor))
    }

    private fun createLayers() {
        val tileRendererLayer = AndroidUtil.createTileRendererLayer(this.tileCaches[0],
                this.mapView!!.model.mapViewPosition, getMapFile(), InternalRenderTheme.OSMARENDER, false, true, false)
        this.mapView!!.layerManager!!.layers.add(tileRendererLayer)
    }

    private fun getMapFile(): MapDataStore {
        return MapFile(File(getMapFileDirectory(), "germany.map"))
    }

    private fun getMapFileDirectory(): File {
        val dir = getExternalStorageDirectory(activity!!)
        return File(dir + "/Maps")
    }

    private fun getRootOfExternalStorage(file: File, context: Context): String =
            file.absolutePath.replace("/Android/data/${context.packageName}/files".toRegex(), "")

    private  fun getExternalStorageDirectory(context: Context): String {
        val externalStorageFiles = ContextCompat.getExternalFilesDirs(context, null)
        return externalStorageFiles.map { getRootOfExternalStorage(it, context) }.filter { !it.contains("emulated") }.first()
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (location.hasBearing()) {
                val affe = 2
                val aff = affe +8
            }
            recentLocation = location
            if (setLocation) {
                if (mapView != null) {
                    mapView!!.setCenter(LatLong(location.latitude, location.longitude))
                    if (::center.isInitialized)
                        mapView!!.layerManager!!.layers.remove(center)
                    center = LocationMarker(LatLong(location.latitude, location.longitude))
                    mapView!!.layerManager!!.layers.add(center)
                }
            }
        }

        override fun onProviderEnabled(p0: String?) {
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        }

        override fun onProviderDisabled(p0: String?) {
        }
    }

    companion object {
        val LOCATION_REFRESH_TIME = 1000L
        val LOCATION_REFRESH_DISTANCE = 0.0F
    }

    private lateinit var preferencesFacade: PreferencesFacade
    private var mapView: MapView? = null
    private var tileCaches: MutableList<TileCache> = ArrayList()
    private lateinit var locationManager: LocationManager
    private var setLocation = true
    private var recentLocation: Location? = null

    private lateinit var center: LocationMarker
}