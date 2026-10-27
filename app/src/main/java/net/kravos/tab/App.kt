package net.kravos.tab

import android.app.Application

class App : Application() {
    lateinit var auth: GoogleAuth
        private set
    lateinit var store: SongStore
        private set
    lateinit var drive: DriveApi
        private set

    override fun onCreate() {
        super.onCreate()
        auth = GoogleAuth(this)
        Http.init(this, auth)
        drive = DriveApi(Http.client())
        store = SongStore(this)
    }
}
