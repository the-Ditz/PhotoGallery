package com.tutorial.bnr.ditzlern.photogallery

import android.content.Context
import android.preference.PreferenceManager
import java.util.prefs.PreferenceChangeEvent

private const val PREF_SEARCH_QUERY = "searchQuery"
object QueryPreferences {

    fun getStoredQuery(context: Context) : String {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY, "")

    }

    fun setStoredQuery(context: Context, query: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply()
    }

}