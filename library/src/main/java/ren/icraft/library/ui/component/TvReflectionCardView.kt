package ren.icraft.library.ui.component

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import ren.icraft.library.R
import androidx.core.graphics.createBitmap

/**
 * TV 卡片倒影
 *
 * 布局高度 = 卡片高度 + 间距 + 倒影高度
 * 倒影在卡片外面，底部圆角正常保留
 * 支持 android:foreground 作为选中框（只作用在卡片上，倒影可选择是否镜像）
 */
class TvReflectionCardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    private val cardView: CardView = CardView(context, attrs, defStyleAttr)

    /** 用来暂存构造阶段提前到来的 foreground */
    private var pendingForeground: Drawable? = null

    var reflectionRatio: Float = 0.111f
        set(value) {
            field = value.coerceIn(0.05f, 0.4f)
            requestLayout()
            invalidate()
        }

    var reflectionGapRatio: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 0.1f)
            requestLayout()
            invalidate()
        }

    var reflectionStartAlpha: Int = 0x78
        set(value) {
            field = value.coerceIn(0, 255)
            invalidate()
        }

    var reflectionShowFocus: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    private var contentHeight = 0
    private var gapHeight = 0
    private var reflectionHeight = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val flipMatrix = Matrix().apply { preScale(1f, -1f) }
    private val xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)

    init {
        addView(cardView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        context.obtainStyledAttributes(attrs, R.styleable.TvReflectionCardView).let {
            reflectionRatio = it.getFloat(R.styleable.TvReflectionCardView_reflectionRatio, 0.111f)
            reflectionGapRatio = it.getFloat(R.styleable.TvReflectionCardView_reflectionGapRatio, 0f)
            reflectionStartAlpha = it.getInt(R.styleable.TvReflectionCardView_reflectionStartAlpha, 0x78)
            reflectionShowFocus = it.getBoolean(R.styleable.TvReflectionCardView_reflectionShowFocus, false)
            it.recycle()
        }

        // 读取 android:foreground
        val ta = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.foreground))
        val fg = ta.getDrawable(0)
        ta.recycle()
        if(fg != null) cardView.foreground = fg

        // 补上可能提前到来的 foreground
        pendingForeground?.let {
            cardView.foreground = it
            pendingForeground = null
        }

        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true

        setWillNotDraw(false)
        clipChildren = false
        clipToPadding = false
    }

    // ==================== 拦截 foreground ====================
    override fun setForeground(foreground: Drawable?) {
        try {
            cardView.foreground = foreground
        } catch (e: Exception) {
            pendingForeground = foreground
        }
    }

    override fun getForeground(): Drawable? {
        return cardView.foreground ?: pendingForeground
    }

    // ==================== 状态同步 ====================
    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        syncStateToCard()
        invalidate()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        syncStateToCard()
        invalidate()
    }

    private fun syncStateToCard() {
        cardView.isSelected = isFocused || isSelected
        cardView.isPressed = isPressed
        cardView.refreshDrawableState()
    }

    // ==================== 子 View 转发 ====================
    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (child === cardView) {
            super.addView(child, index, params)
        } else {
            cardView.addView(child, params)
        }
    }

    override fun removeView(view: View?) {
        if (view === cardView) {
            super.removeView(view)
        } else {
            cardView.removeView(view)
        }
    }

    override fun removeAllViews() {
        cardView.removeAllViews()
    }

    // ==================== 测量 ====================
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        if (heightMode == MeasureSpec.EXACTLY && heightSize > 0) {
            val extraRatio = reflectionRatio + reflectionGapRatio
            val extraHeight = (heightSize * extraRatio).toInt()
            contentHeight = heightSize - extraHeight
            gapHeight = (contentHeight * reflectionGapRatio).toInt()
            reflectionHeight = extraHeight - gapHeight
            if (reflectionHeight < 0) reflectionHeight = 0

            val contentHeightSpec = MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.EXACTLY)
            measureChild(cardView, widthMeasureSpec, contentHeightSpec)

            setMeasuredDimension(
                resolveSize(cardView.measuredWidth, widthMeasureSpec),
                heightSize
            )
        } else {
            measureChild(cardView, widthMeasureSpec, heightMeasureSpec)
            contentHeight = cardView.measuredHeight
            gapHeight = (contentHeight * reflectionGapRatio).toInt()
            reflectionHeight = (contentHeight * reflectionRatio).toInt()

            setMeasuredDimension(
                resolveSize(cardView.measuredWidth, widthMeasureSpec),
                contentHeight + gapHeight + reflectionHeight
            )
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        cardView.layout(0, 0, measuredWidth, contentHeight)
    }

    // ==================== 绘制 ====================
    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        drawReflection(canvas)
    }

    private fun drawReflection(canvas: Canvas) {
        if(reflectionHeight <= 0 || contentHeight <= 0 || width <= 0) return
        if(cardView.width <= 0 || cardView.height <= 0) return

        // 1. 截取内部卡片
        val source = createBitmap(cardView.width, cardView.height)
        val tempCanvas = Canvas(source)

        if(reflectionShowFocus) cardView.draw(tempCanvas)
        else {
            val oldForeground = cardView.foreground
            cardView.foreground = null
            cardView.draw(tempCanvas)
            cardView.foreground = oldForeground
        }

        // 2. 垂直翻转
        val flipped = Bitmap.createBitmap(source, 0, 0, source.width, source.height, flipMatrix, false)

        // 3. 生成渐变倒影
        val reflection = createBitmap(width, reflectionHeight)
        val rCanvas = Canvas(reflection)
        rCanvas.drawBitmap(flipped, 0f, 0f, null)

        val gradient = LinearGradient(
            0f, 0f,
            0f, reflectionHeight.toFloat(),
            Color.argb(reflectionStartAlpha, 255, 255, 255),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        paint.xfermode = xfermode
        rCanvas.drawRect(0f, 0f, width.toFloat(), reflectionHeight.toFloat(), paint)
        paint.shader = null
        paint.xfermode = null

        // 4. 画到卡片下方
        canvas.drawBitmap(reflection, 0f, (contentHeight + gapHeight).toFloat(), null)
        source.recycle()
        flipped.recycle()
        reflection.recycle()
    }

    fun setCardBackgroundColor(color: Int) = cardView.setCardBackgroundColor(color)

    fun setRadius(radius: Float) {
        cardView.radius = radius
    }

    fun setCardElevation(elevation: Float) {
        cardView.cardElevation = elevation
    }

    val card: CardView get() = cardView
}