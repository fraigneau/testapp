package com.example.testapp.spotify

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

data class Tokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresInSec: Long,
    val obtainedAtMillis: Long,
)

class TokenStore(ctx: Context) {
    private val sp: SharedPreferences =
        ctx.getSharedPreferences("spotify_tokens", Context.MODE_PRIVATE)

    fun save(t: Tokens) {
        sp.edit {
            putString("access", t.accessToken)
                .putString("refresh", t.refreshToken)
                .putLong("expires", t.expiresInSec)
                .putLong("obtained", t.obtainedAtMillis)
        }
    }

    fun load(): Tokens? {
        val a = sp.getString("access", null) ?: return null
        val r = sp.getString("refresh", null) ?: return null
        val e = sp.getLong("expires", 0)
        val o = sp.getLong("obtained", 0)
        return Tokens(a, r, e, o)
    }

    fun clear() { sp.edit { clear() } }

    fun isExpired(t: Tokens, now: Long = System.currentTimeMillis()): Boolean {
        return now - t.obtainedAtMillis >= (t.expiresInSec - 30) * 1000
    }
}