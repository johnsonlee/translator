package io.johnsonlee.android.translate.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams

private const val TAG = "DraggableLayout"

open class DraggableLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var initX: () -> Int = { 0 }
    var initY: () -> Int = { 0 }

    private var x: Int = 0
    private var y: Int = 0
    private var layouted: Boolean = false
    private var dragStar: Boolean = false
    private var clickListener: OnClickListener? = null
    private var dragListener: OnDragListener? = null
    private var longClickListener: OnLongClickListener? = null


    init {
        super.setOnLongClickListener { v ->
            dragStar = longClickListener?.onLongClick(v) ?: layoutParams is MarginLayoutParams
            if (dragStar) {
                dragListener?.onDragStart(v)
            }
            Log.i(TAG, "drag start $dragStar")
            dragStar
        }
    }

    var onClick: View.OnClickListener?
        get() = null
        set(value) {
            super.setOnClickListener(value)
        }

    var onDrag: OnDragListener?
        get() = dragListener
        set(value) {
            dragListener = value
        }

    private val boundary: IntArray
        get() {
            val p = parent as View
            val hp = p.paddingLeft + p.paddingRight
            val vp = p.paddingTop + p.paddingBottom
            val right = p.width - hp - width
            val bottom = p.height - vp - height
            return intArrayOf(0, 0, right, bottom)
        }

    override fun setOnLongClickListener(listener: OnLongClickListener?) {
        longClickListener = listener
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (!layouted) {
            val (l, t, r, b) = boundary
            post {
                updateLayoutParams<MarginLayoutParams> {
                    leftMargin = initX().coerceAtLeast(l).coerceAtMost(r)
                    topMargin = initY().coerceAtLeast(t).coerceAtMost(b)
                }
            }
        }
        layouted = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            x = event.x.toInt()
            y = event.y.toInt()
        }

        if (dragStar) {
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val (_, _, right, bottom) = boundary
                    updateLayoutParams<MarginLayoutParams> {
                        topMargin = (topMargin + event.y.toInt() - y).coerceAtLeast(0).coerceAtMost(bottom)
                        leftMargin = (leftMargin + event.x.toInt() - x).coerceAtLeast(0).coerceAtMost(right)
                    }
                    dragListener?.onDragging(this, event)
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    dragStar = false
                    dragListener?.onDragEnd(this)
                }
            }
        }

        return super.onTouchEvent(event)
    }

    interface OnDragListener {
        fun onDragStart(v: View) = Unit
        fun onDragging(v: View, event: MotionEvent) = Unit
        fun onDragEnd(v: View) = Unit
    }

}