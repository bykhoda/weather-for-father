package com.bykhavoy.ehat

import android.app.Application

/** One of the four classes. Holds the hand-wired [AppGraph]. */
class EhatApp : Application() {
    lateinit var graph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        graph = AppGraph(this)
    }
}
