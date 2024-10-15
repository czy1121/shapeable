package me.reezy.cosmo.shapeable

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.LinearLayout
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.Shapeable

class ShapeableLinearLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr, defStyleRes), Shapeable {

    val shape = ShapeableDrawable(context, attrs, defStyleAttr, defStyleRes)

    init {
        background = shape
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (!shape.clipPath.isEmpty) {
            canvas.save()
            canvas.clipPath(shape.clipPath)
            super.dispatchDraw(canvas)
            canvas.restore()
        } else {
            super.dispatchDraw(canvas)
        }
    }

    override fun getShapeAppearanceModel() = shape.shapeAppearanceModel

    override fun setShapeAppearanceModel(shapeAppearanceModel: ShapeAppearanceModel) {
        shape.shapeAppearanceModel = shapeAppearanceModel
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        shape.bounds = Rect(0, 0, w, h)
        invalidate()
    }
}