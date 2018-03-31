package com.gmail.uwriegel.superfit.maps

import android.os.Handler
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Scroller
import org.mapsforge.core.model.LatLong
import org.mapsforge.core.model.Point

class TouchGestureHandler(private val mapView: MapView) : GestureDetector.SimpleOnGestureListener(), ScaleGestureDetector.OnScaleGestureListener, Runnable {
    private val flinger: Scroller
    private var flingLastX: Int = 0
    private var flingLastY: Int = 0
    private var focusX: Float = 0.toFloat()
    private var focusY: Float = 0.toFloat()
    private val handler = Handler()
    private var isInDoubleTap: Boolean = false
    private var isInScale: Boolean = false
    private var pivot: LatLong? = null
    /**
     * Get state of scale gestures:<br></br>
     * - Scale<br></br>
     * - Scale with focus<br></br>
     * - Quick scale (double tap + swipe)
     */
    /**
     * Set state of scale gestures:<br></br>
     * - Scale<br></br>
     * - Scale with focus<br></br>
     * - Quick scale (double tap + swipe)
     */
    var isScaleEnabled = true
    private var scaleFactorCumulative: Float = 0.toFloat()

    init {
        this.flinger = Scroller(mapView.context)
    }

    fun destroy() {
        this.handler.removeCallbacksAndMessages(null)
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        val action = e.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN -> this.isInDoubleTap = true
            MotionEvent.ACTION_UP ->
                // Quick scale in between (cancel double tap)
                if (this.isInDoubleTap) {
                    val mapViewPosition = this.mapView.model.mapViewPosition
                    if (mapViewPosition.zoomLevel < mapViewPosition.zoomLevelMax) {
                        val center = this.mapView.model.mapViewDimension.dimension.center
                        val zoomLevelDiff: Byte = 1
                        val moveHorizontal = (center.x - e.x) / Math.pow(2.0, zoomLevelDiff.toDouble())
                        val moveVertical = (center.y - e.y) / Math.pow(2.0, zoomLevelDiff.toDouble())
                        val pivot = this.mapView.mapViewProjection.fromPixels(e.x.toDouble(), e.y.toDouble())
                        if (pivot != null) {
                            mapViewPosition.pivot = pivot
                            mapViewPosition.moveCenterAndZoom(moveHorizontal, moveVertical, zoomLevelDiff)
                        }
                    }
                    this.isInDoubleTap = false
                    return true
                }
        }

        return false
    }

    override fun onDown(e: MotionEvent): Boolean {
        this.isInScale = false
        this.flinger.forceFinished(true)
        return true
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        if (!this.isInScale && e1.pointerCount == 1 && e2.pointerCount == 1) {
            this.flinger.fling(0, 0, (-velocityX).toInt(), (-velocityY).toInt(), Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)
            this.flingLastY = 0
            this.flingLastX = this.flingLastY
            this.handler.removeCallbacksAndMessages(null)
            this.handler.post(this)
            return true
        }
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        // Normal or quick scale (no long press)
        if (!this.isInScale && !this.isInDoubleTap) {
            mapView.onCenter()
            val tapXY = Point(e.x.toDouble(), e.y.toDouble())
            val tapLatLong = this.mapView.mapViewProjection.fromPixels(tapXY.x, tapXY.y)
            if (tapLatLong != null) {
                for (i in this.mapView.layerManager!!.layers.size() - 1 downTo 0) {
                    val layer = this.mapView.layerManager!!.layers.get(i)
                    val layerXY = this.mapView.mapViewProjection.toPixels(layer.position)
                    if (layer.onLongPress(tapLatLong, layerXY, tapXY)) {
                        break
                    }
                }
            }
        }
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        this.scaleFactorCumulative *= detector.scaleFactor
        this.mapView.model.mapViewPosition.pivot = pivot
        this.mapView.model.mapViewPosition.setScaleFactorAdjustment(scaleFactorCumulative.toDouble())
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        if (!isScaleEnabled) {
            return false
        }

        this.isInScale = true
        this.scaleFactorCumulative = 1f

        // Quick scale (no pivot)
        if (this.isInDoubleTap) {
            this.pivot = null
        } else {
            this.focusX = detector.focusX
            this.focusY = detector.focusY
            this.pivot = this.mapView.mapViewProjection.fromPixels(focusX.toDouble(), focusY.toDouble())
        }
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        val zoomLevelOffset = Math.log(this.scaleFactorCumulative.toDouble()) / Math.log(2.0)
        val zoomLevelDiff: Byte
        if (Math.abs(zoomLevelOffset) > 1) {
            // Complete large zooms towards gesture direction
            zoomLevelDiff = Math.round(if (zoomLevelOffset < 0) Math.floor(zoomLevelOffset) else Math.ceil(zoomLevelOffset)).toByte()
        } else {
            zoomLevelDiff = Math.round(zoomLevelOffset).toByte()
        }

        val mapViewPosition = this.mapView.model.mapViewPosition
        if (zoomLevelDiff.toInt() != 0 && pivot != null) {
            // Zoom with focus
            var moveHorizontal = 0.0
            var moveVertical = 0.0
            val center = this.mapView.model.mapViewDimension.dimension.center
            if (zoomLevelDiff > 0) {
                // Zoom in
                for (i in 1..zoomLevelDiff) {
                    if (mapViewPosition.zoomLevel + i > mapViewPosition.zoomLevelMax) {
                        break
                    }
                    moveHorizontal += (center.x - focusX) / Math.pow(2.0, i.toDouble())
                    moveVertical += (center.y - focusY) / Math.pow(2.0, i.toDouble())
                }
            } else {
                // Zoom out
                for (i in -1 downTo zoomLevelDiff) {
                    if (mapViewPosition.zoomLevel + i < mapViewPosition.zoomLevelMin) {
                        break
                    }
                    moveHorizontal -= (center.x - focusX) / Math.pow(2.0, (i + 1).toDouble())
                    moveVertical -= (center.y - focusY) / Math.pow(2.0, (i + 1).toDouble())
                }
            }
            mapViewPosition.pivot = pivot
            mapViewPosition.moveCenterAndZoom(moveHorizontal, moveVertical, zoomLevelDiff)
        } else {
            // Zoom without focus
            mapViewPosition.zoom(zoomLevelDiff)
        }

        this.isInDoubleTap = false
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        if (!this.isInScale && e1.pointerCount == 1 && e2.pointerCount == 1) {
            this.mapView.model.mapViewPosition.moveCenter((-distanceX).toDouble(), (-distanceY).toDouble(), false)
            this.mapView.onMove()
            return true
        }
        return false
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        val tapXY = Point(e.x.toDouble(), e.y.toDouble())
        val tapLatLong = this.mapView.mapViewProjection.fromPixels(tapXY.x, tapXY.y)
        if (tapLatLong != null) {
            for (i in this.mapView.layerManager!!.layers.size() - 1 downTo 0) {
                val layer = this.mapView.layerManager!!.layers.get(i)
                val layerXY = this.mapView.mapViewProjection.toPixels(layer.position)
                if (layer.onTap(tapLatLong, layerXY, tapXY)) {
                    return true
                }
            }
        }
        return false
    }

    override fun run() {
        val flingerRunning = !this.flinger.isFinished && this.flinger.computeScrollOffset()
        this.mapView.model.mapViewPosition.moveCenter((this.flingLastX - this.flinger.currX).toDouble(), (this.flingLastY - this.flinger.currY).toDouble())
        this.flingLastX = this.flinger.currX
        this.flingLastY = this.flinger.currY
        if (flingerRunning) {
            this.handler.post(this)
        }
    }
}
