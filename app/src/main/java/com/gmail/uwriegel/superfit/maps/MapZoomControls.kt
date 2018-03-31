package com.gmail.uwriegel.superfit.maps

import android.content.Context
import android.os.Handler
import android.os.Message
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.AlphaAnimation
import android.widget.LinearLayout
import android.widget.ZoomButton
import android.widget.ZoomControls
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.model.common.Observer

class MapZoomControls(context: Context, private val mapView: MapView) : LinearLayout(context), Observer {

    private var autoHide: Boolean = false
    private val buttonZoomIn: ZoomButton
    private val buttonZoomOut: ZoomButton
    /**
     * @return true if the zoom controls are visible, false otherwise.
     */
    /**
     * @param showMapZoomControls true if the zoom controls should be visible, false otherwise.
     */
    var isShowMapZoomControls: Boolean = false
    private var zoomControlsGravity: Int = 0
    private val zoomControlsHideHandler: Handler
    private var zoomLevelMax: Byte = 0
    private var zoomLevelMin: Byte = 0

    /**
     * @return true if the zoom controls hide automatically, false otherwise.
     */
    /**
     * @param autoHide true if the zoom controls hide automatically, false otherwise.
     */
    var isAutoHide: Boolean
        get() = this.autoHide
        set(autoHide) {
            this.autoHide = autoHide
            if (!this.autoHide) {
                showZoomControls()
            }
        }

    enum class Orientation private constructor(val layoutOrientation: Int, val zoomInFirst: Boolean) {
        /**
         * Horizontal arrangement, 'zoom in' left of 'zoom out'.
         */
        HORIZONTAL_IN_OUT(LinearLayout.HORIZONTAL, true),

        /**
         * Horizontal arrangement, 'zoom in' right of 'zoom out'.
         */
        HORIZONTAL_OUT_IN(LinearLayout.HORIZONTAL, false),

        /**
         * Vertical arrangement, 'zoom in' above 'zoom out'.
         */
        VERTICAL_IN_OUT(LinearLayout.VERTICAL, true),

        /**
         * Vertical arrangement, 'zoom in' below 'zoom out'.
         */
        VERTICAL_OUT_IN(LinearLayout.VERTICAL, false)
    }

    init {
        this.autoHide = true
        setMarginHorizontal(DEFAULT_HORIZONTAL_MARGIN)
        setMarginVertical(DEFAULT_VERTICAL_MARGIN)
        this.isShowMapZoomControls = true
        this.zoomLevelMax = DEFAULT_ZOOM_LEVEL_MAX
        this.zoomLevelMin = DEFAULT_ZOOM_LEVEL_MIN
        visibility = View.GONE
        this.zoomControlsGravity = DEFAULT_ZOOM_CONTROLS_GRAVITY

        this.zoomControlsHideHandler = object : Handler() {
            override fun handleMessage(message: Message) {
                this@MapZoomControls.hide()
            }
        }

        // Hack to get default zoom buttons
        val defaultZoomControls = ZoomControls(context)
        buttonZoomIn = defaultZoomControls.getChildAt(1) as ZoomButton
        buttonZoomOut = defaultZoomControls.getChildAt(0) as ZoomButton
        defaultZoomControls.removeAllViews()
        orientation = defaultZoomControls.orientation
        setZoomInFirst(false)

        setZoomSpeed(DEFAULT_ZOOM_SPEED)
        buttonZoomIn.setOnClickListener { this@MapZoomControls.mapView.model.mapViewPosition.zoomIn() }
        buttonZoomOut.setOnClickListener { this@MapZoomControls.mapView.model.mapViewPosition.zoomOut() }

        this.mapView.model.mapViewPosition.addObserver(this)
    }

    private fun changeZoomControls(newZoomLevel: Int) {
        this.buttonZoomIn.isEnabled = newZoomLevel < this.zoomLevelMax
        this.buttonZoomOut.isEnabled = newZoomLevel > this.zoomLevelMin
    }

    fun destroy() {
        this.mapView.model.mapViewPosition.removeObserver(this)
    }

    private fun fade(visibility: Int, startAlpha: Float, endAlpha: Float) {
        val anim = AlphaAnimation(startAlpha, endAlpha)
        anim.duration = 500
        startAnimation(anim)
        setVisibility(visibility)
    }

    /**
     * @return the current gravity for the placing of the zoom controls.
     * @see Gravity
     */
    fun getZoomControlsGravity(): Int {
        return this.zoomControlsGravity
    }

    /**
     * @return the maximum zoom level of the map.
     */
    fun getZoomLevelMax(): Byte {
        return this.zoomLevelMax
    }

    /**
     * @return the minimum zoom level of the map.
     */
    fun getZoomLevelMin(): Byte {
        return this.zoomLevelMin
    }

    fun hide() {
        fade(View.GONE, 1.0f, 0.0f)
    }

    override fun onChange() {
        this.onZoomLevelChange(this.mapView.model.mapViewPosition.zoomLevel.toInt())
    }

    fun onMapViewTouchEvent(event: MotionEvent) {
        if (event.pointerCount > 1) {
            // no multitouch
            return
        }
        if (this.isShowMapZoomControls && this.autoHide) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> showZoomControls()
                MotionEvent.ACTION_CANCEL -> showZoomControlsWithTimeout()
                MotionEvent.ACTION_UP -> showZoomControlsWithTimeout()
            }
        }
    }

    fun onZoomLevelChange(newZoomLevel: Int) {
        // to allow changing zoom level programmatically, i.e. not just
        // by user interaction
        if (AndroidUtil.currentThreadIsUiThread()) {
            changeZoomControls(newZoomLevel)
        } else {
            this.mapView.post { changeZoomControls(newZoomLevel) }
        }
    }

    fun setMarginHorizontal(marginHorizontal: Int) {
        setPadding(marginHorizontal, paddingTop, marginHorizontal, paddingBottom)
        mapView.requestLayout()
    }

    fun setMarginVertical(marginVertical: Int) {
        setPadding(paddingLeft, marginVertical, paddingRight, marginVertical)
        mapView.requestLayout()
    }

    /**
     * Sets the gravity for the placing of the zoom controls.
     *
     * @param zoomControlsGravity a combination of [Gravity] constants describing the desired placement.
     */
    fun setZoomControlsGravity(zoomControlsGravity: Int) {
        this.zoomControlsGravity = zoomControlsGravity
        mapView.requestLayout()
    }

    /**
     * Set orientation of zoom controls.
     *
     * @param orientation one of the four orientations.
     */
    fun setZoomControlsOrientation(orientation: Orientation) {
        setOrientation(orientation.layoutOrientation)
        setZoomInFirst(orientation.zoomInFirst)
    }

    /**
     * For horizontal orientation, "zoom in first" means the zoom in button will appear on top of the zoom out button.<br></br>
     * For vertical orientation, "zoom in first" means the zoom in button will appear to the left of the zoom out
     * button.
     *
     * @param zoomInFirst zoom in button will be first in layout.
     */
    fun setZoomInFirst(zoomInFirst: Boolean) {
        this.removeAllViews()
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        if (zoomInFirst) {
            this.addView(buttonZoomIn, layoutParams)
            this.addView(buttonZoomOut, layoutParams)
        } else {
            this.addView(buttonZoomOut, layoutParams)
            this.addView(buttonZoomIn, layoutParams)
        }
    }

    /**
     * Set background drawable of the zoom in button.
     *
     * @param resId resource id of drawable.
     */
    fun setZoomInResource(resId: Int) {
        buttonZoomIn.setBackgroundResource(resId)
    }

    /**
     * Sets the maximum zoom level of the map.
     *
     *
     * The maximum possible zoom level of the MapView depends also on other elements. For example, downloading map tiles
     * may only be possible up to a certain zoom level. Setting a higher maximum zoom level has no effect in this case.
     *
     * @param zoomLevelMax the maximum zoom level.
     * @throws IllegalArgumentException if the maximum zoom level is smaller than the current minimum zoom level.
     */
    fun setZoomLevelMax(zoomLevelMax: Byte) {
        if (zoomLevelMax < this.zoomLevelMin) {
            throw IllegalArgumentException()
        }
        this.zoomLevelMax = zoomLevelMax
    }

    /**
     * Sets the minimum zoom level of the map.
     *
     * @param zoomLevelMin the minimum zoom level.
     * @throws IllegalArgumentException if the minimum zoom level is larger than the current maximum zoom level.
     */
    fun setZoomLevelMin(zoomLevelMin: Byte) {
        if (zoomLevelMin > this.zoomLevelMax) {
            throw IllegalArgumentException()
        }
        this.zoomLevelMin = zoomLevelMin
    }

    /**
     * Set background drawable of the zoom out button.
     *
     * @param resId resource id of drawable.
     */
    fun setZoomOutResource(resId: Int) {
        buttonZoomOut.setBackgroundResource(resId)
    }

    /**
     * Set auto-repeat delay of the zoom buttons.
     *
     * @param ms delay in ms.
     */
    fun setZoomSpeed(ms: Long) {
        buttonZoomIn.setZoomSpeed(ms)
        buttonZoomOut.setZoomSpeed(ms)
    }

    fun show() {
        fade(View.VISIBLE, 0.0f, 1.0f)
    }

    private fun showZoomControls() {
        this.zoomControlsHideHandler.removeMessages(MSG_ZOOM_CONTROLS_HIDE)
        if (visibility != View.VISIBLE) {
            this.show()
        }
    }

    private fun showZoomControlsWithTimeout() {
        showZoomControls()
        this.zoomControlsHideHandler.sendEmptyMessageDelayed(MSG_ZOOM_CONTROLS_HIDE, ZOOM_CONTROLS_TIMEOUT)
    }

    companion object {

        /**
         * Default [Gravity] of the zoom controls.
         */
        private val DEFAULT_ZOOM_CONTROLS_GRAVITY = Gravity.BOTTOM or Gravity.RIGHT

        /**
         * Default maximum zoom level.
         */
        private val DEFAULT_ZOOM_LEVEL_MAX: Byte = 22

        /**
         * Default minimum zoom level.
         */
        private val DEFAULT_ZOOM_LEVEL_MIN: Byte = 0

        /**
         * Auto-repeat delay of the zoom buttons in ms.
         */
        private val DEFAULT_ZOOM_SPEED: Long = 500

        /**
         * Message code for the handler to hide the zoom controls.
         */
        private val MSG_ZOOM_CONTROLS_HIDE = 0

        /**
         * Horizontal margin for the zoom controls.
         */
        private val DEFAULT_HORIZONTAL_MARGIN = 5

        /**
         * Vertical margin for the zoom controls.
         */
        private val DEFAULT_VERTICAL_MARGIN = 0

        /**
         * Delay in milliseconds after which the zoom controls disappear.
         */
        private val ZOOM_CONTROLS_TIMEOUT = ViewConfiguration.getZoomControlsTimeout()
    }
}
