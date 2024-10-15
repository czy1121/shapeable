package me.reezy.cosmo.shapeable

import com.google.android.material.shape.EdgeTreatment
import com.google.android.material.shape.ShapePath

class ArrowEdgeTreatment(
    private val size: Float,
    private val offset: Float = 0f,
    private val edge: Int = EDGE_BOTTOM,
    private val align: Int = ALIGN_CENTER,
) : EdgeTreatment() {
    override fun getEdgePath(length: Float, center: Float, interpolation: Float, shapePath: ShapePath) {
        val isTopOrRight = edge == EDGE_TOP || edge == EDGE_RIGHT
        val newCenter = when (align) {
            ALIGN_START -> if (isTopOrRight) offset else (length - offset)
            ALIGN_END -> if (isTopOrRight) (length - offset) else offset
            else -> if (isTopOrRight) (center + offset) else (center - offset)
        }

        shapePath.lineTo(newCenter - size * interpolation, 0f, newCenter, -size * interpolation)
        shapePath.lineTo(newCenter + size * interpolation, 0f, length, 0f)
    }


    companion object {
        const val ALIGN_START = 1
        const val ALIGN_CENTER = 2
        const val ALIGN_END = 3

        const val EDGE_LEFT = 1
        const val EDGE_TOP = 2
        const val EDGE_RIGHT = 3
        const val EDGE_BOTTOM = 4
    }
}