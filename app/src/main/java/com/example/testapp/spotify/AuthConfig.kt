package com.example.testapp.spotify

object AuthConfig {
    const val CLIENT_ID = "1e33471abadd423a8649bca5aea15715"
    const val REDIRECT_URI = "testapp://callback"
    const val AUTH_URL = "https://accounts.spotify.com/authorize"
    const val TOKEN_URL = "https://accounts.spotify.com/api/"
    const val SCOPES = "user-read-currently-playing user-read-playback-state"
}