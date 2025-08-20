package com.example.testapp.spotify

import com.squareup.moshi.Json
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

data class TokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "expires_in") val expiresIn: Long,
    @Json(name = "refresh_token") val refreshToken: String?,
    @Json(name = "scope") val scope: String?,
)

interface SpotifyAuthApi {
    @FormUrlEncoded
    @POST("token")
    suspend fun exchangeCode(
        @Field("client_id") clientId: String,
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("code_verifier") codeVerifier: String,
    ): TokenResponse

    @FormUrlEncoded
    @POST("token")
    suspend fun refresh(
        @Field("client_id") clientId: String,
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String,
    ): TokenResponse
}