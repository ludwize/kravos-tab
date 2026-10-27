package net.kravos.tab

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class NotAuthorizedException : IOException("Google Drive nije povezan")

/** Adds the Google bearer token to Drive API requests and retries once on 401. */
class AuthInterceptor(private val auth: GoogleAuth) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        if (req.url.host != DriveApi.HOST || req.header("Authorization") != null) return chain.proceed(req)
        val token = auth.tokenBlocking() ?: throw NotAuthorizedException()
        val resp = chain.proceed(req.newBuilder().header("Authorization", "Bearer $token").build())
        if (resp.code != 401) return resp
        resp.close()
        auth.invalidate(token)
        val fresh = auth.tokenBlocking() ?: throw NotAuthorizedException()
        return chain.proceed(req.newBuilder().header("Authorization", "Bearer $fresh").build())
    }
}

object Http {
    const val TAG = "KravosTab"
    @Volatile private var client: OkHttpClient? = null

    fun init(context: Context, auth: GoogleAuth) {
        if (client != null) return
        client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(auth))
            .build()
    }

    fun client(): OkHttpClient = client ?: OkHttpClient()
}
