package com.example.testapp.spotify

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Header

data class CurrentlyPlayingItem(
    @Json(name = "item") val item: Track?,
    @Json(name = "is_playing") val isPlaying: Boolean?,
    @Json(name = "progress_ms") val progressMs: Long?
)
data class Track(
    @Json(name = "name") val name: String?,
    @Json(name = "artists") val artists: List<Artist>?,
    @Json(name = "album") val album: Album?
)
data class Artist(@Json(name = "name") val name: String?)
data class Album(
    @Json(name = "name") val name: String?,
    @Json(name = "images") val images: List<Image>?
)
data class Image(@Json(name = "url") val url: String?)

interface SpotifyApi {
    @GET("v1/me/player/currently-playing")
    suspend fun currentlyPlaying(
        @Header("Authorization") bearer: String
    ): retrofit2.Response<CurrentlyPlayingItem>
}