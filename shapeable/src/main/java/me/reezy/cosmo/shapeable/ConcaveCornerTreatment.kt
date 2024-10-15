package me.reezy.cosmo.shapeable

import android.graphics.RectF
import com.google.android.material.shape.CornerSize
import com.google.android.material.shape.CornerTreatment
import com.google.android.material.shape.ShapePath

class ConcaveCornerTreatment : CornerTreatment() {
    override fun getCornerPath(shapePath: ShapePath, angle: Float, interpolation: Float, bounds: RectF, size: CornerSize) {
        val radius = size.getCornerSize(bounds) * interpolation
        shapePath.reset(0f, radius, 180f, 180 - angle)
        shapePath.lineTo(radius, radius)
        shapePath.lineTo(radius, 0f)
    }
}