package net.kravos.tab

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.WorkerThread
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import java.util.concurrent.TimeUnit

/**
 * Google Drive authorization via Google Play services (AuthorizationClient), same as Kravos Player.
 * No client ID or secret in the app: Google matches the Android OAuth client by package name + signing SHA-1.
 */
class GoogleAuth(private val context: Context) {
    private val client = Identity.getAuthorizationClient(context)
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    val request: AuthorizationRequest = AuthorizationRequest.builder()
        .setRequestedScopes(listOf(Scope(DRIVE_READONLY)))
        .build()

    @Volatile private var token: String? = null
    @Volatile private var tokenAt: Long = 0L
    private val lock = Any()

    val connected: Boolean get() = prefs.getBoolean("connected", false)
    var account: String?
        get() = prefs.getString("account", null)
        set(v) { prefs.edit().putString("account", v).apply() }

    fun authorize(): Task<AuthorizationResult> = client.authorize(request)

    fun resultFromIntent(data: Intent?): AuthorizationResult = client.getAuthorizationResultFromIntent(data)

    fun onAuthorized(result: AuthorizationResult) {
        val t = result.accessToken ?: return
        synchronized(lock) {
            token = t
            tokenAt = System.currentTimeMillis()
        }
        prefs.edit().putBoolean("connected", true).apply()
    }

    /** A valid access token, or null when not connected / consent is needed. Never on the main thread. */
    @WorkerThread
    fun tokenBlocking(): String? {
        if (!connected) return null
        token?.let { if (System.currentTimeMillis() - tokenAt < TOKEN_TTL_MS) return it }
        synchronized(lock) {
            token?.let { if (System.currentTimeMillis() - tokenAt < TOKEN_TTL_MS) return it }
            return try {
                val result = Tasks.await(client.authorize(request), 30, TimeUnit.SECONDS)
                if (result.hasResolution()) null else {
                    token = result.accessToken
                    tokenAt = System.currentTimeMillis()
                    token
                }
            } catch (e: Exception) {
                Log.w(Http.TAG, "authorize failed", e)
                null
            }
        }
    }

    @WorkerThread
    fun invalidate(bad: String) {
        synchronized(lock) {
            if (token == bad) {
                token = null
                tokenAt = 0
            }
        }
        runCatching { GoogleAuthUtil.clearToken(context, bad) }
    }

    companion object {
        const val DRIVE_READONLY = "https://www.googleapis.com/auth/drive.readonly"
        private const val TOKEN_TTL_MS = 40L * 60 * 1000
    }
}
