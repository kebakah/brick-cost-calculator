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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    // Must be registered before the Activity is STARTED, so it lives as a property, not inside onCreate's body.
    private val openBackupFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        try {
            val content = contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream)).readText()
            } ?: throw IllegalStateException("Could not open the selected file")
            val jsLiteral = JSONObject.quote(content)
            webView.post {
                webView.evaluateJavascript("window.receiveImportedBackup($jsLiteral);", null)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Could not read backup file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /** Exposed to the page's JS as `window.AndroidExport`. */
    inner class ExportBridge {
        @JavascriptInterface
        fun shareFile(filename: String, content: String, mimeType: String) {
            runOnUiThread {
                try {
                    val exportsDir = File(cacheDir, "exports").apply { mkdirs() }
                    val file = File(exportsDir, filename)
                    file.writeText(content)
                    val uri: Uri = FileProvider.getUriForFile(this@MainActivity, "$packageName.fileprovider", file)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = mimeType
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(intent, "Export data"))
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        @JavascriptInterface
        fun pickBackupFile() {
            runOnUiThread {
                openBackupFileLauncher.launch(arrayOf("*/*"))
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
