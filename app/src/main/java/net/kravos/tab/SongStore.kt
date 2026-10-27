package net.kravos.tab

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class Song(val id: String, val name: String, val modified: String, val local: Boolean)

/**
 * Songs known to the app: the Drive folder listing (last successful one, so the list works offline)
 * plus files opened from the phone. Every downloaded file is kept, so a song opened once plays offline.
 */
class SongStore(context: Context) {
    private val dir = File(context.filesDir, "songs").apply { mkdirs() }
    private val index = File(context.filesDir, "songs.json")

    @Synchronized
    fun all(): List<Song> = runCatching {
        val a = JSONArray(index.readText())
        (0 until a.length()).map { a.getJSONObject(it) }.map {
            Song(it.getString("id"), it.getString("name"), it.optString("modified"), it.optBoolean("local"))
        }
    }.getOrDefault(emptyList())

    @Synchronized
    private fun save(list: List<Song>) {
        val a = JSONArray()
        list.forEach { a.put(JSONObject().put("id", it.id).put("name", it.name).put("modified", it.modified).put("local", it.local)) }
        index.writeText(a.toString())
    }

    /** Replace the Drive part of the list; drop cached files whose Drive copy changed or disappeared. */
    @Synchronized
    fun setDrive(files: List<DriveFile>) {
        val old = all().filter { !it.local }.associateBy { it.id }
        val keep = files.map { Song(it.id, it.name, it.modifiedTime, false) }
        keep.forEach { s -> old[s.id]?.let { if (it.modified != s.modified) file(s.id).delete() } }
        (old.keys - keep.map { it.id }.toSet()).forEach { file(it).delete() }
        save(keep + all().filter { it.local })
    }

    @Synchronized
    fun addLocal(name: String, bytes: ByteArray): Song {
        val s = Song("local-" + System.currentTimeMillis(), name, "", true)
        file(s.id).writeBytes(bytes)
        save(all() + s)
        return s
    }

    @Synchronized
    fun remove(id: String) {
        file(id).delete()
        save(all().filter { it.id != id })
    }

    fun cached(id: String): ByteArray? = file(id).takeIf { it.exists() && it.length() > 0 }?.readBytes()
    fun put(id: String, bytes: ByteArray) = file(id).writeBytes(bytes)
    fun isCached(id: String) = file(id).exists()

    private fun file(id: String) = File(dir, id.replace(Regex("[^A-Za-z0-9_-]"), "_"))
}
