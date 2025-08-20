package com.example.testapp.spotify

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.example.testapp.spotify.AuthConfig.AUTH_URL
import com.example.testapp.spotify.AuthConfig.CLIENT_ID
import com.example.testapp.spotify.AuthConfig.REDIRECT_URI
import com.example.testapp.spotify.AuthConfig.SCOPES
import androidx.core.net.toUri
import androidx.core.content.edit

class AuthManager(private val ctx: Context) {
    var lastVerifier: String? = null

    fun startLogin() {
        val verifier = Pkce.codeVerifier()
        val challenge = Pkce.codeChallengeS256(verifier)
        lastVerifier = verifier
        ctx.getSharedPreferences("spotify_auth_tmp", Context.MODE_PRIVATE)
            .edit { putString("code_verifier", verifier) }

        val uri = AuthConfig.AUTH_URL.toUri().buildUpon()
            .appendQueryParameter("client_id", AuthConfig.CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", AuthConfig.REDIRECT_URI)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("scope", AuthConfig.SCOPES)
            .build()

        val customTabs = CustomTabsIntent.Builder().build()

        if (ctx !is Activity) {
            customTabs.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        customTabs.launchUrl(ctx, uri)
    }


    fun isRedirect(intent: Intent): Boolean =
        intent.action == Intent.ACTION_VIEW &&
                intent.data != null &&
                intent.data?.scheme == REDIRECT_URI.toUri().scheme &&
                intent.data?.host == REDIRECT_URI.toUri().host

    fun readAuthCode(intent: Intent): String? =
        intent.data?.getQueryParameter("code")

    companion object {
        fun consumeStoredVerifier(ctx: Context): String? {
            val sp = ctx.getSharedPreferences("spotify_auth_tmp", Context.MODE_PRIVATE)
            val v = sp.getString("code_verifier", null)
            if (v != null) sp.edit { remove("code_verifier") }
            return v
        }
    }
}