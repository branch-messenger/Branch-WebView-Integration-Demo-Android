package com.branch.webview.onboarding

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_onboarding_webview.*
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*


class OnboardingWebviewActivity : AppCompatActivity() {
    private var cameraPermission: PermissionRequest? = null
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var cameraPhotoPath: String? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_INPUT_FILE -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (filePathCallback == null) {
                        super.onActivityResult(requestCode, resultCode, data)
                        return
                    }

                    data?.dataString?.let {
                        arrayOf(Uri.parse(it))
                    } ?: run {
                        cameraPhotoPath?.let {
                            arrayOf(Uri.parse(cameraPhotoPath))
                        }
                    }?.let {
                        filePathCallback?.onReceiveValue(it)
                        filePathCallback = null
                    }
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
                return
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_onboarding_webview)

        webview.settings.apply {
            javaScriptEnabled = true
        }

        val encodedCallbackURL = URLEncoder.encode(CALLBACK_URL, StandardCharsets.UTF_8.toString())

        webview.loadUrl("https://accounts-dev.branchapp.com?embedded=native&org=100019&callbackUrl=$encodedCallbackURL")

        webview.webViewClient = object : WebViewClient() {

            @SuppressWarnings("deprecation")
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                url: String?
            ) = url?.let { handleUrl(it) } ?: false

            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ) = request?.url?.toString()?.let { handleUrl(it) } ?: false
        }

        webview.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                super.onPermissionRequest(request)

                if (request?.origin?.toString()
                        ?.equals("https://accounts-dev.branchapp.com") == true
                ) {
                    ActivityCompat.requestPermissions(
                        this@OnboardingWebviewActivity,
                        arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST
                    )
                    cameraPermission = request
                } else {
                    request?.deny()
                }
            }

            override fun onShowFileChooser(
                webView: WebView?,
                newFilePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {

                filePathCallback?.onReceiveValue(null)

                filePathCallback = newFilePathCallback

                var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                takePictureIntent?.resolveActivity(packageManager)?.let {
                    var photoFile: File? = null

                    val timeStamp =
                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

                    val imageFileName = "JPEG_${timeStamp}_"

                    val storageDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

                    try {
                        photoFile = File.createTempFile(imageFileName, ".jpg", storageDir)
                    } catch (ex: IOException) {
                        //Error creating file
                    }

                    photoFile?.let {
                        cameraPhotoPath = "file:${photoFile.absolutePath}"
                        takePictureIntent?.putExtra(
                            MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile)
                        )
                    } ?: run {
                        takePictureIntent = null
                    }
                }

                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)

                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)

                contentSelectionIntent.type = "image/*"

                val intentArray = takePictureIntent?.let {
                    arrayOf(it)
                } ?: run {
                    emptyArray<Intent>()
                }

                val chooserIntent = Intent(Intent.ACTION_CHOOSER)

                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)

                startActivityForResult(chooserIntent, REQUEST_CODE_INPUT_FILE)

                return true
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<out String>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraPermission?.grant(cameraPermission?.resources)
            } else {
                cameraPermission?.deny()
            }
        }
    }

    private fun handleUrl(url: String): Boolean {
        if (url == CALLBACK_URL) {
            setResult(Activity.RESULT_OK)
            finish()
            return true
        }

        return false
    }


    companion object {
        private const val CALLBACK_URL = "https://www.mycallbackurl.com"
        private const val REQUEST_CODE_INPUT_FILE = 1
        private const val CAMERA_PERMISSION_REQUEST = 1111
    }
}