package io.johnsonlee.android.translate

import android.view.View
import android.view.ViewGroup
import java.util.Stack

inline fun <reified T : View> View.findViewByType(): Array<T> {
    val views = mutableListOf<T>()
    val stack = Stack<View>()
    stack.push(this)

    while (stack.isNotEmpty()) {
        when (val v = stack.pop()) {
            is ViewGroup -> {
                for (i in 0 until v.childCount) {
                    stack.push(v.getChildAt(i))
                }
            }
            else -> if (v is T) {
                views += v
            }
        }
    }

    return views.toTypedArray()
}