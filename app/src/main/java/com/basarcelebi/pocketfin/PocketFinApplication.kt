package com.basarcelebi.pocketfin

import android.app.Application
import com.basarcelebi.pocketfin.database.PocketFinDatabase

class PocketFinApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PocketFinDatabase.getDatabase(this)
    }
}
