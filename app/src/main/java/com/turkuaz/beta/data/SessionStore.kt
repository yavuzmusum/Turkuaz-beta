package com.turkuaz.beta.data

import android.content.Context
import android.content.SharedPreferences

/** Access/refresh token ve rolun yerel saklanmasi (basit SharedPreferences - MVP). */
class SessionStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("turkuaz_session", Context.MODE_PRIVATE)

    var accessToken: String?
        get() = prefs.getString("access_token", null)
        set(v) = prefs.edit().putString("access_token", v).apply()

    var refreshToken: String?
        get() = prefs.getString("refresh_token", null)
        set(v) = prefs.edit().putString("refresh_token", v).apply()

    var role: String?
        get() = prefs.getString("role", null)
        set(v) = prefs.edit().putString("role", v).apply()

    val isLoggedIn: Boolean get() = accessToken != null

    fun clear() {
        prefs.edit().clear().apply()
    }
}
