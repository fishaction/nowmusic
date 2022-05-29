package com.example.refactoringnowmusic.tasks

import android.os.AsyncTask
import com.example.refactoringnowmusic.MediaAppDetails

abstract class FindMediaAppsTask constructor(
        private val callback: AppListUpdatedCallback, private val sortAlphabetical: Boolean
) : AsyncTask<Void, Void, List<MediaAppDetails>>() {

    /**
     * Callback used by [FindMediaAppsTask].
     */
    interface AppListUpdatedCallback {
        fun onAppListUpdated(mediaAppEntries: List<MediaAppDetails>)
    }

    protected abstract val mediaApps: List<MediaAppDetails>

    override fun doInBackground(vararg params: Void): List<MediaAppDetails> {
        val mediaApps = ArrayList(mediaApps)
        if(sortAlphabetical) {
            // Sort the list by localized app name for convenience.
            mediaApps.sortWith(Comparator { left, right ->
                left.appName.compareTo(right.appName, ignoreCase = true)
            })
        }
        return mediaApps
    }

    override fun onPostExecute(mediaAppEntries: List<MediaAppDetails>) {
        callback.onAppListUpdated(mediaAppEntries)
    }
}