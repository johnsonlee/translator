package io.johnsonlee.android.translate.app

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        findViewById<TextView>(R.id.txt_greeting).setOnClickListener {
            Toast.makeText(this, (it as TextView).text, Toast.LENGTH_SHORT).show()
        }
    }

}