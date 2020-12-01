package com.branch.webview.onboarding

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        btn_begin_onboarding.setOnClickListener {
            startActivityForResult(
                Intent(this, OnboardingWebviewActivity::class.java),
                REQUEST_CODE_LAUNCH_ONBOARDING
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            REQUEST_CODE_LAUNCH_ONBOARDING -> {
                if(resultCode == Activity.RESULT_OK){
                    AlertDialog.Builder(this)
                        .setTitle("Success!")
                        .setMessage("Onboarding complete!")
                        .setPositiveButton("Okay"){ dialog, which ->
                            dialog.dismiss()
                        }
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_LAUNCH_ONBOARDING = 0
    }
}