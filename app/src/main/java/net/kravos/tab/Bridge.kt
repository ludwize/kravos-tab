package net.kravos.tab

import android.util.Base64
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/** The page's window.KravosNative. Answers come back asynchronously through window.KT.* callbacks. */
class Bridge(
    private val app: App,
    private val web: WebView,
    private val scope: CoroutineScope,
    private val actions: Actions,
) {
    interface Actions {
        fun connect()
        fun openLocal()
    }

    @Volatile private var pendingUpdate: Release? = null

    private fun js(code: String) = web.post { web.evaluateJavascript(code, null) }
    private fun q(s: String?) = if (s == null) "null" else JSONObject.quote(s)

    private fun songsJson(): String {
        val a = JSONArray()
        app.store.all().forEach {
            a.put(JSONObject().put("id", it.id).put("name", it.name).put("local", it.local).put("cached", app.store.isCached(it.id)))
        }
        return a.toString()
    }

    fun stateJson(): String = JSONObject()
        .put("connected", app.auth.connected)
        .put("account", app.auth.account ?: "")
        .put("version", BuildConfig.VERSION_NAME)
        .toString()

    fun emitAuth() = js("window.KT&&KT.onAuth(${stateJson()})")

    @JavascriptInterface fun state(): String = stateJson()

    @JavascriptInterface fun connect() = web.post { actions.connect() }

    @JavascriptInterface fun openLocal() = web.post { actions.openLocal() }

    @JavascriptInterface fun refresh() {
        scope.launch(Dispatchers.IO) {
            var err: String? = null
            if (!app.auth.connected) {
                err = if (app.store.all().isEmpty()) "Poveži Google Drive (⚙ gore desno) ili otvori fajl sa telefona." else null
            } else try {
                if (app.auth.account == null) runCatching { app.auth.account = app.drive.about() }
                val folders = app.drive.findFolders(DriveApi.FOLDER_NAME)
                if (folders.isEmpty()) err = "Na Drive-u nema foldera „${DriveApi.FOLDER_NAME}“. Napravi ga i ubaci .gp fajlove."
                else {
                    val files = folders.flatMap { app.drive.tabFiles(it) }.distinctBy { it.id }
                    app.store.setDrive(files)
                    if (files.isEmpty()) err = "Folder „${DriveApi.FOLDER_NAME}“ je prazan. Ubaci u njega .gp fajlove."
                }
            } catch (e: NotAuthorizedException) {
                err = "Google Drive traži ponovnu prijavu (⚙ → Poveži Google Drive)."
            } catch (e: Exception) {
                Log.w(Http.TAG, "refresh failed", e)
                err = "Drive nije dostupan, prikazujem sačuvane pesme."
            }
            js("window.KT&&KT.onSongs(${songsJson()},${q(err)})")
            emitAuth()
        }
    }

    @JavascriptInterface fun load(id: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val bytes = app.store.cached(id) ?: app.drive.download(id).also { app.store.put(id, it) }
                js("window.KT&&KT.onSong(${q(id)},${q(Base64.encodeToString(bytes, Base64.NO_WRAP))})")
            } catch (e: Exception) {
                Log.w(Http.TAG, "load failed", e)
                js("window.KT&&KT.onError(${q("Pesma nije preuzeta. Proveri internet i pokušaj ponovo.")})")
            }
        }
    }

    fun onLocalFile(name: String, bytes: ByteArray) {
        scope.launch(Dispatchers.IO) {
            val s = app.store.addLocal(name, bytes)
            js("window.KT&&(KT.onSongs(${songsJson()},null),KT.onLocalAdded(${q(s.id)}))")
        }
    }

    @JavascriptInterface fun removeLocal(id: String) {
        scope.launch(Dispatchers.IO) {
            app.store.remove(id)
            js("window.KT&&KT.onSongs(${songsJson()},null)")
        }
    }

    @JavascriptInterface fun checkUpdate() {
        scope.launch {
            val r = Updater.latest()
            pendingUpdate = r?.takeIf { Updater.isNewer(it) }
            val info = pendingUpdate?.let { JSONObject().put("name", "1.0.${it.build}").toString() } ?: "null"
            js("window.KT&&KT.onUpdate($info)")
        }
    }

    @JavascriptInterface fun installUpdate() {
        val r = pendingUpdate ?: return
        scope.launch {
            try {
                val apk = Updater.download(app, r) { p -> js("window.KT&&KT.onUpdateProgress($p)") }
                web.post { Updater.install(app, apk) }
            } catch (e: Exception) {
                js("window.KT&&KT.onError(${q("Ažuriranje nije preuzeto: ${e.message}")})")
            }
        }
    }
}
