package com.kibrom.brickcost

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    /** Exposed to the page's JS as `window.AndroidExport` so it can hand off a generated file to the native Share sheet. */
    inner class ExportBridge {
        @JavascriptInterface
        fun shareFile(filename: String, content: String) {
            runOnUiThread {
                try {
                    val exportsDir = File(cacheDir, "exports").apply { mkdirs() }
                    val file = File(exportsDir, filename)
                    file.writeText(content)
                    val uri: Uri = FileProvider.getUriForFile(this@MainActivity, "$packageName.fileprovider", file)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/vnd.ms-excel"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(intent, "Export data"))
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()
        webView.addJavascriptInterface(ExportBridge(), "AndroidExport")
        webView.loadUrl("file:///android_asset/index.html")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
