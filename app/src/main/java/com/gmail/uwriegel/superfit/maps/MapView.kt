package com.gmail.uwriegel.superfit.maps

import android.content.Context
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.*
import org.mapsforge.core.model.BoundingBox
import org.mapsforge.core.model.Dimension
import org.mapsforge.core.model.LatLong
import org.mapsforge.core.util.Parameters
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.controller.FrameBufferController
import org.mapsforge.map.controller.LayerManagerController
import org.mapsforge.map.controller.MapViewController
import org.mapsforge.map.layer.Layer
import org.mapsforge.map.layer.LayerManager
import org.mapsforge.map.layer.TileLayer
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.model.Model
import org.mapsforge.map.model.common.Observer
import org.mapsforge.map.scalebar.DefaultMapScaleBar
import org.mapsforge.map.scalebar.MapScaleBar
import org.mapsforge.map.util.MapPositionUtil
import org.mapsforge.map.util.MapViewProjection
import org.mapsforge.map.view.FpsCounter
import org.mapsforge.map.view.FrameBuffer
import org.mapsforge.map.view.FrameBufferHA
import org.mapsforge.map.view.FrameBufferHA2

class MapView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : ViewGroup(context, attributeSet), org.mapsforge.map.view.MapView, Observer {

    /**
     * Child view Layout information associated with MapView.
     */
    class LayoutParams : ViewGroup.LayoutParams {

        /**
         * The location of the child view within the map view.
         */
        var latLong: LatLong? = null

        /**
         * The alignment of the view compared to the location.
         */
        lateinit var alignment: Alignment

        /**
         * Special values for the alignment requested by child views.
         */
        enum class Alignment {
            TOP_LEFT, TOP_CENTER, TOP_RIGHT, CENTER_LEFT, CENTER, CENTER_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
        }

        constructor(c: Context, attrs: AttributeSet) : super(c, attrs) {
            this.alignment = LayoutParams.Alignment.BOTTOM_CENTER
        }

        /**
         * Creates a new set of layout parameters for a child view of MapView.
         *
         * @param width     the width of the child, either [.MATCH_PARENT], [.WRAP_CONTENT] or a fixed size in pixels.
         * @param height    the height of the child, either [.MATCH_PARENT], [.WRAP_CONTENT] or a fixed size in pixels.
         * @param latLong   the location of the child within the map view.
         * @param alignment the alignment of the view compared to the location.
         */
        constructor(width: Int, height: Int, latLong: LatLong?, alignment: Alignment) : super(width, height) {
            this.latLong = latLong
            this.alignment = alignment
        }

        constructor(source: ViewGroup.LayoutParams) : super(source) {}
    }

    fun setLocationSetter(setter: LocationSetter) {
        this.setter = setter
    }

    override fun addLayer(layer: Layer) {
        this.layerManager!!.layers.add(layer)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is MapView.LayoutParams
    }

    fun onCenter() {
        setter.changeValue(true)
    }

    fun onMove() {
        setter.changeValue(false)
    }
    /**
     * Clear map view.
     */
    override fun destroy() {
        this.touchGestureHandler.destroy()
        this.layoutHandler.removeCallbacksAndMessages(null)
        this.layerManager!!.finish()
        this.layerManager = null
        this.frameBufferController.destroy()
        this.frameBuffer.destroy()
        if (this.mapScaleBar != null) {
            this.mapScaleBar!!.destroy()
        }
        this.mapZoomControls.destroy()
        this.getModel().mapViewPosition.destroy()
    }

    /**
     * Clear all map view elements.<br></br>
     * i.e. layers, tile cache, label store, map view, resources, etc.
     */
    override fun destroyAll() {
        for (layer in this.layerManager!!.layers) {
            this.layerManager!!.layers.remove(layer)
            layer.onDestroy()
            if (layer is TileLayer<*>) {
                layer.tileCache.destroy()
            }
            if (layer is TileRendererLayer) {
                val labelStore = layer.labelStore
                labelStore?.clear()
            }
        }
        destroy()
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return MapView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, null, MapView.LayoutParams.Alignment.BOTTOM_CENTER)
    }

    override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams {
        return MapView.LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return MapView.LayoutParams(p)
    }

    override fun getBoundingBox(): BoundingBox {
        return MapPositionUtil.getBoundingBox(this.model.mapViewPosition.mapPosition,
                dimension, this.model.displayModel.tileSize)
    }

    override fun getDimension(): Dimension {
        return Dimension(width, height)
    }

    override fun getFpsCounter(): FpsCounter {
        return this.fpsCounter
    }

    override fun getFrameBuffer(): FrameBuffer {
        return this.frameBuffer
    }

    override fun getLayerManager(): LayerManager? {
        return this.layerManager
    }

    override fun getMapScaleBar(): MapScaleBar? {
        return this.mapScaleBar
    }

    override fun getMapViewProjection(): MapViewProjection {
        return this.mapViewProjection
    }

    override fun getModel(): Model {
        return this.model
    }

    override fun onChange() {
        // Request layout for child views (besides zoom controls)
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child != this.mapZoomControls) {
                layoutHandler.post { requestLayout() }
                break
            }
        }
    }

    override fun onDraw(androidCanvas: Canvas) {
        val graphicContext = AndroidGraphicFactory.createGraphicContext(androidCanvas)
        this.frameBuffer.draw(graphicContext)
        if (this.mapScaleBar != null) {
            this.mapScaleBar!!.draw(graphicContext)
        }
        this.fpsCounter.draw(graphicContext)
        graphicContext.destroy()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        // Zoom controls
        if (this.mapZoomControls.visibility != View.GONE) {
            val childGravity = this.mapZoomControls.getZoomControlsGravity()
            val childWidth = this.mapZoomControls.measuredWidth
            val childHeight = this.mapZoomControls.measuredHeight

            val childLeft: Int
            when (childGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.LEFT -> childLeft = left
                Gravity.CENTER_HORIZONTAL -> childLeft = left + (right - left - childWidth) / 2
                Gravity.RIGHT -> childLeft = right - childWidth
                else -> childLeft = right - childWidth
            }

            val childTop: Int
            when (childGravity and Gravity.VERTICAL_GRAVITY_MASK) {
                Gravity.TOP -> childTop = top
                Gravity.CENTER_VERTICAL -> childTop = top + (bottom - top - childHeight) / 2
                Gravity.BOTTOM -> childTop = bottom - childHeight
                else -> childTop = bottom - childHeight
            }

            this.mapZoomControls.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
        }

        // Child views (besides zoom controls)
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child == this.mapZoomControls) {
                continue
            }
            if (child.visibility != View.GONE && checkLayoutParams(child.layoutParams)) {
                val params = child.layoutParams as MapView.LayoutParams
                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight
                val point = mapViewProjection.toPixels(params.latLong)
                if (point != null) {
                    var childLeft = paddingLeft + Math.round(point.x).toInt()
                    var childTop = paddingTop + Math.round(point.y).toInt()
                    when (params.alignment) {
                        MapView.LayoutParams.Alignment.TOP_LEFT -> {
                        }
                        MapView.LayoutParams.Alignment.TOP_CENTER -> childLeft -= childWidth / 2
                        MapView.LayoutParams.Alignment.TOP_RIGHT -> childLeft -= childWidth
                        MapView.LayoutParams.Alignment.CENTER_LEFT -> childTop -= childHeight / 2
                        MapView.LayoutParams.Alignment.CENTER -> {
                            childLeft -= childWidth / 2
                            childTop -= childHeight / 2
                        }
                        MapView.LayoutParams.Alignment.CENTER_RIGHT -> {
                            childLeft -= childWidth
                            childTop -= childHeight / 2
                        }
                        MapView.LayoutParams.Alignment.BOTTOM_LEFT -> childTop -= childHeight
                        MapView.LayoutParams.Alignment.BOTTOM_CENTER -> {
                            childLeft -= childWidth / 2
                            childTop -= childHeight
                        }
                        MapView.LayoutParams.Alignment.BOTTOM_RIGHT -> {
                            childLeft -= childWidth
                            childTop -= childHeight
                        }
                    }
                    child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        this.model.mapViewDimension.dimension = Dimension(width, height)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isClickable) {
            return false
        }

        this.mapZoomControls.onMapViewTouchEvent(event)
        if (this.gestureDetectorExternal != null && this.gestureDetectorExternal!!.onTouchEvent(event)) {
            return true
        }

        var retVal = this.scaleGestureDetector.onTouchEvent(event)
        if (!this.scaleGestureDetector.isInProgress) {
            retVal = this.gestureDetector.onTouchEvent(event)
        }
        return retVal
    }

    override fun repaint() {
        if (Thread.currentThread() === Looper.getMainLooper().thread) {
            invalidate()
        } else {
            postInvalidate()
        }
    }

    /**
     * Sets the visibility of the zoom controls.
     *
     * @param showZoomControls true if the zoom controls should be visible, false otherwise.
     */
    fun setBuiltInZoomControls(showZoomControls: Boolean) {
        this.mapZoomControls.isShowMapZoomControls = showZoomControls
    }

    override fun setCenter(center: LatLong) {
        this.model.mapViewPosition.center = center
    }

    fun setGestureDetector(gestureDetector: GestureDetector) {
        this.gestureDetectorExternal = gestureDetector
    }

    override fun setMapScaleBar(mapScaleBar: MapScaleBar) {
        if (this.mapScaleBar != null) {
            this.mapScaleBar!!.destroy()
        }
        this.mapScaleBar = mapScaleBar
    }

    override fun setZoomLevel(zoomLevel: Byte) {
        this.model.mapViewPosition.zoomLevel = zoomLevel
    }

    override fun setZoomLevelMax(zoomLevelMax: Byte) {
        this.model.mapViewPosition.zoomLevelMax = zoomLevelMax
        this.mapZoomControls.setZoomLevelMax(zoomLevelMax)
    }

    override fun setZoomLevelMin(zoomLevelMin: Byte) {
        this.model.mapViewPosition.zoomLevelMin = zoomLevelMin
        this.mapZoomControls.setZoomLevelMin(zoomLevelMin)
    }

    private val fpsCounter: FpsCounter
    private val frameBuffer: FrameBuffer
    private val frameBufferController: FrameBufferController
    private val gestureDetector: GestureDetector
    private var gestureDetectorExternal: GestureDetector? = null
    private var layerManager: LayerManager? = null
    private val layoutHandler = Handler()
    private var mapScaleBar: MapScaleBar? = null
    private val mapViewProjection: MapViewProjection
    /**
     * @return the zoom controls instance which is used in this MapView.
     */
    val mapZoomControls: MapZoomControls
    private val model: Model
    private val scaleGestureDetector: ScaleGestureDetector
    val touchGestureHandler: TouchGestureHandler
    lateinit var setter: LocationSetter

    init {

        descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        setWillNotDraw(false)

        this.model = Model()

        this.fpsCounter = FpsCounter(GRAPHIC_FACTORY, this.model.displayModel)
        if (Parameters.FRAME_BUFFER_HA2)
            this.frameBuffer = FrameBufferHA2(this.model.frameBufferModel, this.model.displayModel, GRAPHIC_FACTORY)
        else
            this.frameBuffer = FrameBufferHA(this.model.frameBufferModel, this.model.displayModel, GRAPHIC_FACTORY)
        this.frameBufferController = FrameBufferController.create(this.frameBuffer, this.model)

        this.layerManager = LayerManager(this, this.model.mapViewPosition, GRAPHIC_FACTORY)
        this.layerManager!!.start()
        LayerManagerController.create(this.layerManager, this.model)

        MapViewController.create(this, this.model)

        this.touchGestureHandler = TouchGestureHandler(this)
        this.gestureDetector = GestureDetector(context, touchGestureHandler)
        this.scaleGestureDetector = ScaleGestureDetector(context, touchGestureHandler)

        this.mapZoomControls = MapZoomControls(context, this)
        this.addView(this.mapZoomControls, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        this.mapScaleBar = DefaultMapScaleBar(this.model.mapViewPosition, this.model.mapViewDimension,
                GRAPHIC_FACTORY, this.model.displayModel)
        this.mapViewProjection = MapViewProjection(this)

        model.mapViewPosition.addObserver(this)
    }

    companion object {

        private val GRAPHIC_FACTORY = AndroidGraphicFactory.INSTANCE
    }
}
