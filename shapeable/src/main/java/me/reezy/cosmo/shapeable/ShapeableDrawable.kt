package me.reezy.cosmo.shapeable

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import androidx.core.content.res.TypedArrayUtils.obtainAttributes
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toRectF
import com.google.android.material.shadow.ShadowRenderer
import com.google.android.material.shape.AbsoluteCornerSize
import com.google.android.material.shape.CornerSize
import com.google.android.material.shape.CornerTreatment
import com.google.android.material.shape.CutCornerTreatment
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RelativeCornerSize
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.ShapeAppearancePathProvider
import org.xmlpull.v1.XmlPullParser
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.min
import kotlin.math.sqrt

@Suppress("NOTHING_TO_INLINE")
@SuppressLint("RestrictedApi")
class ShapeableDrawable private constructor(state: ShapeableState, msdState: MaterialShapeDrawableState, isNew: Boolean = true) : MaterialShapeDrawable(msdState) {

    private var constantState: RealConstantState = RealConstantState(state, msdState)

    private val clipRect: RectF = RectF()

    val clipPath = Path()

    init {
        fieldShadowRenderer.set(this, RealShadowRenderer())
        if (isNew) {
            shadowCompatibilityMode = SHADOW_COMPAT_MODE_ALWAYS
            setUseTintColorForShadow(true)
            tintList = ColorStateList.valueOf(Color.TRANSPARENT)
        }
    }

    constructor() : this(ShapeableState(), MaterialShapeDrawableState(ShapeAppearanceModel(), null))

    constructor(shapeModel: ShapeAppearanceModel) : this(ShapeableState(), MaterialShapeDrawableState(shapeModel, null))

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

    override fun getIntrinsicWidth(): Int = shapeableState.width
    override fun getIntrinsicHeight(): Int = shapeableState.height
    override fun getConstantState(): ConstantState = constantState

    override fun mutate(): Drawable {
        super.mutate()
        constantState = RealConstantState(ShapeableState(shapeableState), super.getConstantState() as MaterialShapeDrawableState)
        return this
    }

    private val shapeableState: ShapeableState get() = constantState.shapeable

    private fun initAttrs(a: TypedArray) {

        shapeableState.init(a)

        strokeColor = a.getColorStateList(R.styleable.ShapeableDrawable_strokeColor)
        strokeWidth = a.getDimensionPixelSize(R.styleable.ShapeableDrawable_strokeWidth, 0).toFloat()

        val cornerSize = a.getCornerSize(R.styleable.ShapeableDrawable_cornerSize)
        if (cornerSize != null) {
            val cornerPosition = a.getInteger(R.styleable.ShapeableDrawable_cornerPosition, 0)
            val cornerType = a.getInteger(R.styleable.ShapeableDrawable_cornerType, 0)
            setCorners(cornerPosition, cornerSize, cornerType)
        }


        if (shapeableState.gradientColors != null) {
            setUseTintColorForShadow(false)
            tintList = null
        } else {
            setUseTintColorForShadow(true)
            tintList = a.getColorStateList(R.styleable.ShapeableDrawable_backgroundTint) ?: ColorStateList.valueOf(Color.TRANSPARENT)
        }


        // 阴影
        if (a.hasValue(R.styleable.ShapeableDrawable_shadowRadius)) {
            shadowRadius = a.getDimensionPixelSize(R.styleable.ShapeableDrawable_shadowRadius, 0)
        }
        if (a.hasValue(R.styleable.ShapeableDrawable_shadowColor)) {
            setShadowColor(a.getColor(R.styleable.ShapeableDrawable_shadowColor, 0))
        }
        if (a.hasValue(R.styleable.ShapeableDrawable_shadowOffsetX) || a.hasValue(R.styleable.ShapeableDrawable_shadowOffsetY)) {
            val shadowOffsetY = a.getDimensionPixelSize(R.styleable.ShapeableDrawable_shadowOffsetY, 0)
            val shadowOffsetX = a.getDimensionPixelSize(R.styleable.ShapeableDrawable_shadowOffsetX, 0)
            setShadowOffset(shadowOffsetX, shadowOffsetY)
        }


        // 汽泡箭头
        val arrowSize = a.getDimension(R.styleable.ShapeableDrawable_arrowSize, 0f)
        if (arrowSize > 0f) {
            val arrowOffset = a.getDimension(R.styleable.ShapeableDrawable_arrowOffset, 0f)
            val arrowEdge = a.getInteger(R.styleable.ShapeableDrawable_arrowEdge, ArrowEdgeTreatment.EDGE_BOTTOM)
            val arrowAlign = a.getInteger(R.styleable.ShapeableDrawable_arrowAlign, ArrowEdgeTreatment.ALIGN_CENTER)
            setArrow(arrowSize, arrowOffset, arrowEdge, arrowAlign)
        }
    }


    @Suppress("OVERRIDE_DEPRECATION")
    override fun setShadowRadius(radius: Int) {
        @Suppress("DEPRECATION")
        super.setShadowRadius(radius)
    }

    private fun setCorners(position: Int, size: CornerSize, type: Int) {
        val corner = when (type) {
            0 -> RoundedCornerTreatment()
            1 -> CutCornerTreatment()
            2 -> ConcaveCornerTreatment()
            else -> RoundedCornerTreatment()
        }
        val builder = shapeAppearanceModel.toBuilder()
        shapeAppearanceModel = when (position) {
            1 -> builder.tl(corner, size).build()
            2 -> builder.tr(corner, size).build()
            3 -> builder.bl(corner, size).build()
            4 -> builder.br(corner, size).build()

            5 -> builder.tl(corner, size).tr(corner, size).build()
            6 -> builder.bl(corner, size).br(corner, size).build()
            7 -> builder.tl(corner, size).bl(corner, size).build()
            8 -> builder.tr(corner, size).br(corner, size).build()

            9 -> builder.tl(corner, size).br(corner, size).build()
            10 -> builder.bl(corner, size).tr(corner, size).build()

            11 -> builder.tr(corner, size).bl(corner, size).br(corner, size).build()
            12 -> builder.tl(corner, size).bl(corner, size).br(corner, size).build()
            13 -> builder.tl(corner, size).tr(corner, size).br(corner, size).build()
            14 -> builder.tl(corner, size).tr(corner, size).bl(corner, size).build()

            else -> builder.setAllCorners(corner).setAllCornerSizes(size).build()
        }

    }

    private inline fun ShapeAppearanceModel.Builder.tl(cornerTreatment: CornerTreatment, size: CornerSize) =
        setTopLeftCorner(cornerTreatment).setTopLeftCornerSize(size)

    private inline fun ShapeAppearanceModel.Builder.tr(cornerTreatment: CornerTreatment, size: CornerSize) =
        setTopRightCorner(cornerTreatment).setTopRightCornerSize(size)

    private inline fun ShapeAppearanceModel.Builder.bl(cornerTreatment: CornerTreatment, size: CornerSize) =
        setBottomLeftCorner(cornerTreatment).setBottomLeftCornerSize(size)

    private inline fun ShapeAppearanceModel.Builder.br(cornerTreatment: CornerTreatment, size: CornerSize) =
        setBottomRightCorner(cornerTreatment).setBottomRightCornerSize(size)

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

        shapeableState.createFillShader(bounds.toRectF())?.let {
            (fieldFillPaint.get(this) as Paint).shader = it
        }
        invalidateSelf()
    }


    private fun updateClipPath(width: Int, height: Int) {
        val inset = strokeWidth

        clipRect.set(inset, inset, width - inset, height - inset)

        val newModel = shapeAppearanceModel.withTransformedCornerSizes { cs ->
            CornerSize { cs.getCornerSize(it) - inset / sqrt(2f) }
        }

        clipPathProvider.calculatePath(newModel, 1f, clipRect, clipPath)
    }

    private fun TypedArray.getCornerSize(index: Int): CornerSize? {
        val value = peekValue(index) ?: return null
        if (value.type == TypedValue.TYPE_DIMENSION) {
            val size = TypedValue.complexToDimension(value.data, resources.displayMetrics)
            return if (size > 0) AbsoluteCornerSize(size) else null
        }
        if (value.type == TypedValue.TYPE_FRACTION) {
            val fraction = value.getFraction(1.0f, 1.0f)
            return if (fraction > 0) RelativeCornerSize(fraction) else null
        }
        return null
    }


    private class ShapeableState() {


        var width: Int = -1
        var height: Int = -1

        var gradientColors: IntArray? = null
        var gradientType: Int = GradientDrawable.LINEAR_GRADIENT
        var gradientOrientation: GradientDrawable.Orientation = GradientDrawable.Orientation.TL_BR
        var gradientCenterX: Float = 0.5f
        var gradientCenterY: Float = 0.5f
        var gradientRadius: Float = 0.5f

        constructor(orig: ShapeableState) : this() {
            width = orig.width
            height = orig.height

            gradientColors = orig.gradientColors
            gradientType = orig.gradientType
            gradientOrientation = orig.gradientOrientation
            gradientCenterX = orig.gradientCenterX
            gradientCenterY = orig.gradientCenterY
            gradientRadius = orig.gradientRadius
        }


        fun init(a: TypedArray) {
            width = a.getDimensionPixelSize(R.styleable.ShapeableDrawable_android_width, -1)
            height = a.getDimensionPixelSize(R.styleable.ShapeableDrawable_android_height, -1)

            gradientColors = a.getGradientColors()
            gradientType = a.getInt(R.styleable.ShapeableDrawable_gradientType, GradientDrawable.LINEAR_GRADIENT)
            gradientRadius = a.getDimension(R.styleable.ShapeableDrawable_gradientRadius, 0f)
            gradientCenterX = a.getFloat(R.styleable.ShapeableDrawable_gradientCenterX, 0.5f)
            gradientCenterY = a.getFloat(R.styleable.ShapeableDrawable_gradientCenterY, 0.5f)
            gradientOrientation = try {
                GradientDrawable.Orientation.values()[a.getInt(R.styleable.ShapeableDrawable_gradientOrientation, 0)]
            } catch (e: IndexOutOfBoundsException) {
                GradientDrawable.Orientation.TOP_BOTTOM
            }
        }

        fun createFillShader(rect: RectF): Shader? = when {
            gradientColors == null -> null
            gradientType == GradientDrawable.LINEAR_GRADIENT -> {
                val r = when (gradientOrientation) {
                    GradientDrawable.Orientation.TOP_BOTTOM -> RectF(rect.left, rect.top, rect.left, rect.bottom)
                    GradientDrawable.Orientation.TR_BL -> RectF(rect.right, rect.top, rect.left, rect.bottom)
                    GradientDrawable.Orientation.RIGHT_LEFT -> RectF(rect.right, rect.top, rect.left, rect.top)
                    GradientDrawable.Orientation.BR_TL -> RectF(rect.right, rect.bottom, rect.left, rect.top)
                    GradientDrawable.Orientation.BOTTOM_TOP -> RectF(rect.left, rect.bottom, rect.left, rect.top)
                    GradientDrawable.Orientation.BL_TR -> RectF(rect.left, rect.bottom, rect.right, rect.top)
                    GradientDrawable.Orientation.LEFT_RIGHT -> RectF(rect.left, rect.top, rect.right, rect.top)
                    else -> RectF(rect.left, rect.top, rect.right, rect.bottom)
                }
                val tileMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Shader.TileMode.DECAL
                } else {
                    Shader.TileMode.CLAMP
                }
                LinearGradient(r.left, r.top, r.right, r.bottom, gradientColors!!, null, tileMode)
            }

            gradientType == GradientDrawable.RADIAL_GRADIENT -> {
                if (gradientRadius > 0) {
                    val x0 = rect.left + (rect.right - rect.left) * gradientCenterX
                    val y0 = rect.top + (rect.bottom - rect.top) * gradientCenterY
                    RadialGradient(x0, y0, gradientRadius, gradientColors!!, null, Shader.TileMode.CLAMP)
                } else {
                    null
                }
            }

            gradientType == GradientDrawable.SWEEP_GRADIENT -> {
                val x0 = rect.left + (rect.right - rect.left) * gradientCenterX
                val y0 = rect.top + (rect.bottom - rect.top) * gradientCenterY
                SweepGradient(x0, y0, gradientColors!!, null)
            }

            else -> null
        }

        private fun TypedArray.getGradientColors(): IntArray? {

            val hasStartColor = hasValue(R.styleable.ShapeableDrawable_gradientStartColor)
            val hasCenterColor = hasValue(R.styleable.ShapeableDrawable_gradientCenterColor)
            val hasEndColor = hasValue(R.styleable.ShapeableDrawable_gradientEndColor)

            if (hasStartColor || hasCenterColor || hasEndColor) {
                val start = getColor(R.styleable.ShapeableDrawable_gradientStartColor, 0)
                val center = getColor(R.styleable.ShapeableDrawable_gradientCenterColor, 0)
                val end = getColor(R.styleable.ShapeableDrawable_gradientEndColor, 0)
                return if (hasCenterColor) intArrayOf(start, center, end) else intArrayOf(start, end)
            }
            return null
        }

    }


    private class RealConstantState(val shapeable: ShapeableState, val msd: MaterialShapeDrawableState) : ConstantState() {
        override fun newDrawable(): Drawable {
            val d = ShapeableDrawable(ShapeableState(shapeable), msd, false)
            d.invalidateSelf()
            return d
        }

        override fun getChangingConfigurations(): Int = 0
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
        private val fieldFillPaint by field(MaterialShapeDrawable::class.java, "fillPaint")

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