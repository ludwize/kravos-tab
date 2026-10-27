package net.kravos.tab

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class DriveException(val code: Int, message: String) : IOException(message)

data class DriveFile(val id: String, val name: String, val modifiedTime: String, val size: Long)

/** Minimal Drive v3 client: find the "Kravos Tab" folder, list its tab files, download one. */
class DriveApi(private val http: OkHttpClient) {

    fun about(): String? {
        val url = "$BASE/about".toHttpUrl().newBuilder().addQueryParameter("fields", "user(displayName,emailAddress)").build()
        http.newCall(Request.Builder().url(url).build()).execute().use { r ->
            if (!r.isSuccessful) throw DriveException(r.code, r.body?.string() ?: "")
            val user = JSONObject(r.body!!.string()).optJSONObject("user")
            return user?.optString("emailAddress")?.ifEmpty { null } ?: user?.optString("displayName")
        }
    }

    /** All folders named [name] (My Drive and shared), newest first. */
    fun findFolders(name: String): List<String> =
        query("name = '${name.replace("'", "\\'")}' and mimeType = '$FOLDER_MIME' and trashed = false", "modifiedTime desc").map { it.id }

    fun tabFiles(folderId: String): List<DriveFile> =
        query("'$folderId' in parents and trashed = false and mimeType != '$FOLDER_MIME'", "name")
            .filter { it.name.substringAfterLast('.', "").lowercase() in EXTENSIONS }

    fun download(id: String): ByteArray {
        val url = "$BASE/files/$id".toHttpUrl().newBuilder().addQueryParameter("alt", "media").build()
        http.newCall(Request.Builder().url(url).build()).execute().use { r ->
            if (!r.isSuccessful) throw DriveException(r.code, r.body?.string() ?: "")
            return r.body!!.bytes()
        }
    }

    private fun query(q: String, orderBy: String): List<DriveFile> {
        val out = ArrayList<DriveFile>()
        var pageToken: String? = null
        do {
            val b = "$BASE/files".toHttpUrl().newBuilder()
                .addQueryParameter("q", q)
                .addQueryParameter("orderBy", orderBy)
                .addQueryParameter("pageSize", "200")
                .addQueryParameter("supportsAllDrives", "true")
                .addQueryParameter("includeItemsFromAllDrives", "true")
                .addQueryParameter("fields", "nextPageToken,files(id,name,modifiedTime,size)")
            pageToken?.let { b.addQueryParameter("pageToken", it) }
            http.newCall(Request.Builder().url(b.build()).build()).execute().use { r ->
                val body = r.body?.string() ?: ""
                if (!r.isSuccessful) throw DriveException(r.code, body)
                val o = JSONObject(body)
                val files = o.optJSONArray("files")
                if (files != null) for (i in 0 until files.length()) {
                    val f = files.getJSONObject(i)
                    out += DriveFile(f.getString("id"), f.optString("name"), f.optString("modifiedTime"), f.optString("size").toLongOrNull() ?: 0L)
                }
                pageToken = o.optString("nextPageToken").ifEmpty { null }
            }
        } while (pageToken != null)
        return out
    }

    companion object {
        const val HOST = "www.googleapis.com"
        const val BASE = "https://www.googleapis.com/drive/v3"
        const val FOLDER_MIME = "application/vnd.google-apps.folder"
        const val FOLDER_NAME = "Kravos Tab"
        val EXTENSIONS = setOf("gp", "gpx", "gp5", "gp4", "gp3", "xml", "musicxml", "mxl")
    }
}
