package com.example.testapp.spotify

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitBuilder {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun auth(): SpotifyAuthApi =
        Retrofit.Builder()
            .baseUrl(AuthConfig.TOKEN_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SpotifyAuthApi::class.java)

    fun api(): SpotifyApi =
        Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SpotifyApi::class.java)
}