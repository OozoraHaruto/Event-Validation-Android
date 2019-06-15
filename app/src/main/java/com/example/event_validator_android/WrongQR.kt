package com.example.event_validator_android

import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.gms.vision.barcode.Barcode

class WrongQR : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wrong_qr)

        val code                                                            = intent.getParcelableExtra(INT_BARCODE) as Barcode

        findViewById<TextView>(R.id.txtQRData).apply {
            text                                                            = code.displayValue
        }
        Log.e(LOG_KEY, code.rawValue)
        when(code.valueFormat){
            Barcode.URL -> {
                buttonImplement("Open link", Intent.ACTION_VIEW, Uri.parse(code.displayValue))
                setSnideComment(R.string.snide_comment_website, R.string.supported)
            }
        }
    }

    fun buttonImplement(title:String, intent: String, data: Uri){
        findViewById<Button>(R.id.btnPerformAction).apply {
            text                                                            = title
            isEnabled                                                       = true
            isVisible                                                       = true
            setOnClickListener {
                Intent(intent).apply {
                    this.data                                               = data
                }.also {
                    startActivity(it)
                }
            }
        }
    }

    fun setSnideComment(comment:Int, support: Int){
        findViewById<TextView>(R.id.txtSnideComment).apply {
            text                                                            = "${resources.getText(comment)}\n${resources.getText(support)}"
        }
    }
}
