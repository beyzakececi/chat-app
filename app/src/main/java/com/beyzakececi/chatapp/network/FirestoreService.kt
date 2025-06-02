// app/src/main/java/com/beyzakececi/chatapp/network/FirestoreService.kt
package com.beyzakececi.chatapp.network

import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface FirestoreService {
    // 1) Tüm kullanıcıları listele
    @GET("projects/{projectId}/databases/(default)/documents/users")
    fun listUsers(
        @Path("projectId") projectId: String,
        @Header("Authorization") authHeader: String
    ): Call<JsonObject>

    // 2) Belirli bir kullanıcıyı getir
    @GET("projects/{projectId}/databases/(default)/documents/users/{userId}")
    fun getUser(
        @Path("projectId") projectId: String,
        @Path("userId") userId: String,
        @Header("Authorization") authHeader: String
    ): Call<JsonObject>

    // 3) Yeni kullanıcı oluştur
    @POST("projects/{projectId}/databases/(default)/documents/users")
    fun createUser(
        @Path("projectId") projectId: String,
        @Header("Authorization") authHeader: String,
        @Body body: JsonObject
    ): Call<JsonObject>

    // 4) Kullanıcıyı güncelle (sadece email alanı)
    @PATCH("projects/{projectId}/databases/(default)/documents/users/{userId}")
    fun updateUser(
        @Path("projectId") projectId: String,
        @Path("userId") userId: String,
        @Header("Authorization") authHeader: String,
        @Query("updateMask.fieldPaths") updateField: String = "email",
        @Body body: JsonObject
    ): Call<JsonObject>

    // 5) Kullanıcıyı sil
    @DELETE("projects/{projectId}/databases/(default)/documents/users/{userId}")
    fun deleteUser(
        @Path("projectId") projectId: String,
        @Path("userId") userId: String,
        @Header("Authorization") authHeader: String
    ): Call<ResponseBody>
}
