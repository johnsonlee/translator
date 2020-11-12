package io.johnsonlee.android.translate

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class TranslateProvider : ContentProvider(), Application.ActivityLifecycleCallbacks, OnCanceledListener, OnSuccessListener<Any?>, OnFailureListener {

    private val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.KOREAN)
            .setTargetLanguage(TranslateLanguage.CHINESE)
            .build()


    @SuppressLint("ApplySharedPref")
    override fun onCreate(): Boolean {
        Translation.getClient(options)
                .downloadModelIfNeeded(DownloadConditions.Builder().requireWifi().build())
                .addOnCanceledListener(this)
                .addOnSuccessListener(this)
                .addOnFailureListener(this)
        (context?.applicationContext as? Application)?.registerActivityLifecycleCallbacks(this)
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?) = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?) = 0

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity is FragmentActivity) {
            activity.lifecycle.addObserver(TranslateAssistant(activity, options))
        }
    }

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onCanceled() {
        Log.i(TAG, "MlKit downloading canceled")
    }

    override fun onSuccess(o: Any?) {
        Log.i(TAG, "MlKit download success")
    }

    override fun onFailure(e: Exception) {
        val cause = e.cause
        val code = (cause as? MlKitException)?.errorCode ?: MlKitException.UNKNOWN
        Log.e(TAG, "MlKit downloading failed: $code", cause)
    }

    companion object {
        private const val TAG = "TranslateProvider"
    }
}