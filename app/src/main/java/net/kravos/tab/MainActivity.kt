package net.kravos.tab

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.webkit.WebViewAssetLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), Bridge.Actions {
    private lateinit var web: WebView
    private lateinit var bridge: Bridge
    private val app get() = application as App

    private val authLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { res ->
        runCatching { app.auth.onAuthorized(app.auth.resultFromIntent(res.data)) }
            .onFailure { Log.w(Http.TAG, "auth result", it) }
        afterAuth()
    }

    private val pickLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) readLocal(uri)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WebView.setWebContentsDebuggingEnabled(true)

        web = WebView(this)
        setContentView(web)
        ViewCompat.setOnApplyWindowInsetsListener(web) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            v.setPadding(b.left, b.top, b.right, b.bottom)
            WindowInsetsCompat.CONSUMED
        }

        val loader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = false
            allowContentAccess = false
        }
        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
                loader.shouldInterceptRequest(request.url)
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                request.url.host != "appassets.androidplatform.net"
        }
        bridge = Bridge(app, web, lifecycleScope, this)
        web.addJavascriptInterface(bridge, "KravosNative")
        if (savedInstanceState != null) web.restoreState(savedInstanceState)
        else web.loadUrl("https://appassets.androidplatform.net/assets/web/index.html")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        web.saveState(outState)
    }

    override fun connect() {
        app.auth.authorize()
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    val pi = result.pendingIntent
                    if (pi != null) authLauncher.launch(IntentSenderRequest.Builder(pi.intentSender).build())
                } else {
                    app.auth.onAuthorized(result)
                    afterAuth()
                }
            }
            .addOnFailureListener { e ->
                Log.w(Http.TAG, "authorize", e)
                web.evaluateJavascript(
                    "window.KT&&KT.onError(${org.json.JSONObject.quote("Prijava na Google nije uspela: ${e.message}")})", null,
                )
            }
    }

    private fun afterAuth() {
        lifecycleScope.launch(Dispatchers.IO) {
            if (app.auth.connected) runCatching { app.auth.account = app.drive.about() }
            bridge.emitAuth()
        }
    }

    override fun openLocal() {
        pickLauncher.launch(arrayOf("*/*"))
    }

    private fun readLocal(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val name = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
                if (it.moveToFirst()) it.getString(0) else null
            } ?: "pesma.gp"
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@launch
            bridge.onLocalFile(name, bytes)
        }
    }
}
