package com.beyzakececi.chatapp

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val PREFS_NAME = "chat_app_prefs"
    private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
    private const val KEY_LAST_SEEN = "key_last_seen"
    private lateinit var preferences: SharedPreferences

    /** Bu metodu uygulama başlarken çağıracağız. */
    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** Oturum açma durumunu saklamak için Boolean değer. */
    var isLoggedIn: Boolean
        get() = preferences.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = preferences.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    /** Kullanıcının son aktif olduğu zamanı (Unix timestamp) saklamak için Long. */
    var lastSeenTimestamp: Long
        get() = preferences.getLong(KEY_LAST_SEEN, 0L)
        set(value) = preferences.edit().putLong(KEY_LAST_SEEN, value).apply()
}

class ChatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // SharedPreferences’i başlatıyoruz.
        Prefs.init(this)
    }
}
