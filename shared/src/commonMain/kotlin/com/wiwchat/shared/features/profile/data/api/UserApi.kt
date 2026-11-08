package com.wiwchat.shared.features.profile.data.api

import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class UserApi @Inject constructor(
//    private val client: HttpClient,
//    private val tokenDataStore: TokenDataStore
) {
//    companion object {
//        const val BASE_URL = BuildConfig.API_BASE_URL
//    }
//
//    private suspend fun authHeader(): String {
//        return "Bearer ${tokenDataStore.getAccessToken() ?: ""}"
//    }
//
//    suspend fun getProfile(userId: String): UserProfileDto {
//        val response = client.get("$BASE_URL/users/$userId") {
//            header(HttpHeaders.Authorization, authHeader())
//        }
//        return when (response.status) {
//            HttpStatusCode.OK -> response.body()
//            else -> throw Exception("Ошибка загрузки профиля: ${response.status}")
//        }
//    }
//
//    suspend fun getFriendStatus(targetUserId: String): FriendStatusDto {
//        val response = client.get("$BASE_URL/users/friends/status") {
//            header(HttpHeaders.Authorization, authHeader())
//            parameter("targetUserId", targetUserId)
//        }
//        return when (response.status) {
//            HttpStatusCode.OK -> response.body()
//            else -> FriendStatusDto("none")
//        }
//    }
//
//    suspend fun sendFriendRequest(targetUserId: String) {
//        val response = client.post("$BASE_URL/users/friends/request") {
//            header(HttpHeaders.Authorization, authHeader())
//            contentType(ContentType.Application.Json)
//            setBody(FriendRequestDto(targetUserId))
//        }
//        if (response.status != HttpStatusCode.OK) throw Exception("Ошибка отправки заявки")
//    }
//
//    suspend fun blockUser(targetUserId: String, blockMessages: Boolean = true, blockGroupAdd: Boolean = true) {
//        val response = client.post("$BASE_URL/users/blocks") {
//            header(HttpHeaders.Authorization, authHeader())
//            contentType(ContentType.Application.Json)
//            setBody(BlockUserDto(targetUserId, blockMessages, blockGroupAdd))
//        }
//        if (response.status != HttpStatusCode.OK) throw Exception("Ошибка блокировки")
//    }
//
//    suspend fun updateUsername(username: String) {
//        val response = client.put("$BASE_URL/users/username") {
//            header(HttpHeaders.Authorization, authHeader())
//            contentType(ContentType.Application.Json)
//            setBody(SetUsernameRequest(username))
//        }
//        if (response.status != HttpStatusCode.OK) throw Exception("Ошибка обновления имени")
//    }
//
//    suspend fun updateProfile(userId: String, email: String, role: String) {
//        val response = client.put("$BASE_URL/users/$userId") {
//            header(HttpHeaders.Authorization, authHeader())
//            contentType(ContentType.Application.Json)
//            setBody(UpdateUserRequest(email, role))
//        }
//        if (response.status != HttpStatusCode.OK) throw Exception("Ошибка обновления профиля")
//    }
//
//    suspend fun changePassword(userId: String, oldPassword: String, newPassword: String) {
//        val response = client.put("$BASE_URL/users/$userId/password") {
//            header(HttpHeaders.Authorization, authHeader())
//            contentType(ContentType.Application.Json)
//            setBody(ChangePasswordRequest(oldPassword, newPassword))
//        }
//        if (response.status != HttpStatusCode.OK) throw Exception("Ошибка смены пароля")
//    }
//    suspend fun deleteUser(userId: String) {
//        val response = client.delete("$BASE_URL/users/$userId") {
//            header(HttpHeaders.Authorization, authHeader())
//        }
//        if (response.status != HttpStatusCode.OK) throw Exception("Ошибка удаления")
//    }
}