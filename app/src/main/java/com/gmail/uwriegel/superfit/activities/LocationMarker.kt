package com.gmail.uwriegel.superfit.activities

import org.mapsforge.core.graphics.Canvas
import org.mapsforge.core.graphics.Color
import org.mapsforge.core.graphics.GraphicFactory
import org.mapsforge.core.graphics.Style
import org.mapsforge.core.model.BoundingBox
import org.mapsforge.core.model.LatLong
import org.mapsforge.core.model.Point
import org.mapsforge.core.util.MercatorProjection
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.layer.Layer

class LocationMarker(private val center: LatLong) : Layer() {
    @Synchronized
    override fun draw(boundingBox: BoundingBox, zoomLevel: Byte, canvas: Canvas, topLeftPoint: Point) {
        if (boundingBox.contains(center)) {
            val mapSize = MercatorProjection.getMapSize(zoomLevel, displayModel.tileSize)
            val x1 = (MercatorProjection.longitudeToPixelX(center.longitude, mapSize) - topLeftPoint.x).toInt()
            val y1 = (MercatorProjection.latitudeToPixelY(center.latitude, mapSize) - topLeftPoint.y).toInt()

            val paint = AndroidGraphicFactory.INSTANCE.createPaint()
            paint.setStyle(Style.FILL)
            paint.color = AndroidGraphicFactory.INSTANCE.createColor(Color.WHITE)
            canvas.drawCircle(x1, y1, 16, paint)
            paint.strokeWidth = 16F
            paint.setStyle(Style.STROKE)
            paint.color = AndroidGraphicFactory.INSTANCE.createColor(Color.BLACK)
            canvas.drawCircle(x1, y1, 20, paint)
        }
    }
}