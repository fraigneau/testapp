package com.example.testapp.spotify

import android.util.Base64
import java.security.MessageDigest
import kotlin.random.Random

object Pkce {
    fun codeVerifier(length: Int = 64): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~"
        return (1..length).joinToString("") { chars[Random.nextInt(chars.length)].toString() }
    }
    fun codeChallengeS256(verifier: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray())
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }
}