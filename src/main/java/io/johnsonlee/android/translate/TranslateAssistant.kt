package io.johnsonlee.android.translate

import android.content.Context
import android.content.SharedPreferences
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ComponentActivity
import androidx.core.content.edit
import androidx.core.util.set
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import io.johnsonlee.android.translate.widget.DraggableLayout
import java.lang.ref.WeakReference

private const val PREF_NAME = "translate-assistant"
private const val KEY_X = "x"
private const val KEY_Y = "y"

internal class TranslateAssistant(
        private val activity: ComponentActivity,
        private val options: TranslatorOptions
) : LifecycleObserver {

    // View ref => text hash code
    private val cache: SparseArray<Int> = SparseArray(100)

    private val clickListener: View.OnClickListener = View.OnClickListener {
        it.rootView.findViewByType<TextView>().filter(View::isShown).forEach { v ->
            val str = v.text.toString()
            val hash = System.identityHashCode(v)

            if (v.getTag(R.id.TAG_TRANSLATED) == true && str.hashCode() == cache[hash]) {
                return@forEach
            }

            cache[hash] = str.hashCode()
            val ref = WeakReference<TextView>(v)
            translator.translate(v.text.toString()).addOnSuccessListener { result ->
                v.setTag(R.id.TAG_TRANSLATED, true)
                ref.get()?.apply {
                    text = result
                    cache[System.identityHashCode(this)] = result.hashCode()
                }
            }
        }
    }

    private val dragListener: DraggableLayout.OnDragListener = object : DraggableLayout.OnDragListener {
        override fun onDragEnd(v: View) = pref.edit {
            putInt(KEY_X, v.marginLeft)
            putInt(KEY_Y, v.marginTop)
        }
    }

    private val pref: SharedPreferences by lazy {
        activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private val translator: Translator by lazy {
        Translation.getClient(options)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        val content = activity.window.findViewById<ViewGroup>(android.R.id.content) ?: return
        activity.findViewById<ViewGroup>(R.id.google_translate_assistant)?.let {
            return
        }
        val layout = activity.layoutInflater.inflate(R.layout.translate_assistant, content, false)
        layout.findViewById<DraggableLayout>(R.id.google_translate_assistant).apply {
            initX = { pref.getInt(KEY_X, 0) }
            initY = { pref.getInt(KEY_Y, activity.findViewById<View>(android.R.id.content).height shr 1) }
            onClick = clickListener
            onDrag = dragListener
            content.addView(layout)
        }
        layout.bringToFront()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        activity.findViewById<DraggableLayout>(R.id.google_translate_assistant)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        cache.clear()
        translator.close()
    }

}