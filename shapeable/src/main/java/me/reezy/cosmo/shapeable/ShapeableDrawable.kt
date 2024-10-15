package me.reezy.cosmo.shapeable

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import androidx.core.content.res.TypedArrayUtils.obtainAttributes
import androidx.core.graphics.ColorUtils
import com.google.android.material.shadow.ShadowRenderer
import com.google.android.material.shape.CornerSize
import com.google.android.material.shape.CutCornerTreatment
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.ShapeAppearancePathProvider
import me.reezy.cosmo.R
import org.xmlpull.v1.XmlPullParser
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.min
import kotlin.math.sqrt

@SuppressLint("RestrictedApi")
class ShapeableDrawable(shapeModel: ShapeAppearanceModel) : MaterialShapeDrawable(shapeModel) {
    init {
        fieldShadowRenderer.set(this, RealShadowRenderer())
        shadowCompatibilityMode = SHADOW_COMPAT_MODE_ALWAYS
        setUseTintColorForShadow(true)
        tintList = ColorStateList.valueOf(Color.TRANSPARENT)
    }

    val clipPath = Path()

    private val clipRect: RectF = RectF()

    constructor() : this(ShapeAppearanceModel())

    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0)
            : this(ShapeAppearanceModel.builder(context, attrs, defStyleAttr, defStyleRes).build()) {

        val ta = context.obtainStyledAttributes(attrs, R.styleable.ShapeableDrawable, defStyleAttr, defStyleRes)
        initAttrs(ta)
        ta.recycle()
    }

    override fun inflate(r: Resources, parser: XmlPullParser, attrs: AttributeSet, theme: Resources.Theme?) {
        super.inflate(r, parser, attrs, theme)

        val ta = obtainAttributes(r, theme, attrs, R.styleable.ShapeableDrawable)
        initAttrs(ta)
        ta.recycle()
    }

    private fun initAttrs(ta: TypedArray) {
        strokeColor = ta.getColorStateList(R.styleable.ShapeableDrawable_strokeColor)
        strokeWidth = ta.getDimensionPixelSize(R.styleable.ShapeableDrawable_strokeWidth, 0).toFloat()
        tintList = ta.getColorStateList(R.styleable.ShapeableDrawable_backgroundTint) ?: ColorStateList.valueOf(Color.TRANSPARENT)

        if (ta.hasValue(R.styleable.ShapeableDrawable_cornerSize)) {
            setCornerSize(ta.getDimensionPixelSize(R.styleable.ShapeableDrawable_cornerSize, 0).toFloat())
        }

        if (ta.hasValue(R.styleable.ShapeableDrawable_cornerType)) {
            val cornerTreatment = when(ta.getInteger(R.styleable.ShapeableDrawable_cornerType, 0)) {
                0 -> RoundedCornerTreatment()
                1 -> CutCornerTreatment()
                2 -> ConcaveCornerTreatment()
                else -> RoundedCornerTreatment()
            }
            shapeAppearanceModel =  shapeAppearanceModel.toBuilder().setAllCorners(cornerTreatment).build()
        }

        if (ta.hasValue(R.styleable.ShapeableDrawable_shadowRadius)) {
            shadowRadius = ta.getDimensionPixelSize(R.styleable.ShapeableDrawable_shadowRadius, 0)
        }
        if (ta.hasValue(R.styleable.ShapeableDrawable_shadowColor)) {
            setShadowColor(ta.getColor(R.styleable.ShapeableDrawable_shadowColor, 0))
        }


        val shadowOffsetY = ta.getDimensionPixelSize(R.styleable.ShapeableDrawable_shadowOffsetY, 0)
        val shadowOffsetX = ta.getDimensionPixelSize(R.styleable.ShapeableDrawable_shadowOffsetX, 0)
        setShadowOffset(shadowOffsetX, shadowOffsetY)


        val arrowSize = ta.getDimension(R.styleable.ShapeableDrawable_arrowSize, 0f)
        if (arrowSize > 0f) {
            val arrowOffset = ta.getDimension(R.styleable.ShapeableDrawable_arrowOffset, 0f)
            val arrowEdge = ta.getInteger(R.styleable.ShapeableDrawable_arrowEdge,  ArrowEdgeTreatment.EDGE_BOTTOM)
            val arrowAlign = ta.getInteger(R.styleable.ShapeableDrawable_arrowAlign, ArrowEdgeTreatment.ALIGN_CENTER )
            setArrow(arrowSize, arrowOffset, arrowEdge, arrowAlign)
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun setShadowRadius(radius: Int) {
        @Suppress("DEPRECATION")
        super.setShadowRadius(radius)
    }

    fun setShadowOffset(offsetX: Int, offsetY: Int) {
        if (offsetX != 0) {
            shadowCompatRotation = (atan(offsetY / offsetX.toFloat()) * 180 / PI).toInt()
        }
        if (offsetY != 0) {
            shadowVerticalOffset = offsetY
        }
    }

    fun setArrow(size: Float, offset: Float = 0f, edge: Int = ArrowEdgeTreatment.EDGE_BOTTOM, align: Int = ArrowEdgeTreatment.ALIGN_CENTER) {
        if (size > 0) {
            val treatment = ArrowEdgeTreatment(size, offset, edge, align)

            val builder = shapeAppearanceModel.toBuilder()
            shapeAppearanceModel = when (edge) {
                ArrowEdgeTreatment.EDGE_LEFT -> builder.setLeftEdge(treatment).build()
                ArrowEdgeTreatment.EDGE_TOP -> builder.setTopEdge(treatment).build()
                ArrowEdgeTreatment.EDGE_RIGHT -> builder.setRightEdge(treatment).build()
                ArrowEdgeTreatment.EDGE_BOTTOM -> builder.setBottomEdge(treatment).build()
                else -> builder.setBottomEdge(treatment).build()
            }
        }
    }

    override fun setShapeAppearanceModel(shapeAppearanceModel: ShapeAppearanceModel) {
        super.setShapeAppearanceModel(shapeAppearanceModel)
        updateClipPath(bounds.width(), bounds.height())
        invalidateSelf()
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        updateClipPath(bounds.width(), bounds.height())
    }


    private fun updateClipPath(width: Int, height: Int) {
        val inset = strokeWidth

        clipRect.set(inset, inset, width - inset, height - inset)

        val newModel = shapeAppearanceModel.withTransformedCornerSizes { cs ->
            CornerSize { cs.getCornerSize(it) - inset / sqrt(2f) }
        }

        clipPathProvider.calculatePath(newModel, 1f, clipRect, clipPath)
    }

    private class RealShadowRenderer : ShadowRenderer() {
        override fun setShadowColor(shadowColor: Int) {
            val alpha = min(Color.alpha(shadowColor), 0x44)

            val startColor = ColorUtils.setAlphaComponent(shadowColor, alpha)
            val middleColor = ColorUtils.setAlphaComponent(shadowColor, (0.25f * alpha).toInt())
            val endColor = ColorUtils.setAlphaComponent(shadowColor, 0)
            fieldShadowStartColor.set(this, startColor)
            fieldShadowMiddleColor.set(this, middleColor)
            fieldShadowEndColor.set(this, endColor)

            shadowPaint.color = shadowColor
        }
    }

    companion object {

        private val clipPathProvider = ShapeAppearancePathProvider()

        private val fieldShadowRenderer by field(MaterialShapeDrawable::class.java, "shadowRenderer")

        private val fieldShadowStartColor by field(ShadowRenderer::class.java, "shadowStartColor")
        private val fieldShadowMiddleColor by field(ShadowRenderer::class.java, "shadowMiddleColor")
        private val fieldShadowEndColor by field(ShadowRenderer::class.java, "shadowEndColor")

        private fun field(clazz: Class<*>, name: String) = lazy {
            val field = clazz.getDeclaredField(name)
            field.isAccessible = true
            field
        }
    }
}