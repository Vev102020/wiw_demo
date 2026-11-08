package com.wiwchat.shared.features.chat.data.api

import io.ktor.client.HttpClient
import jakarta.inject.Inject
import jakarta.inject.Singleton


@Singleton
class ChatApi @Inject constructor(
    private val client: HttpClient,
) {
//
//    private fun authHeader(): String {
//        val token = runBlocking { getAccessToken() }
//        return if (!token.isNullOrBlank()) {
//            "Bearer $token"
//        } else {
//            throw Exception("Токен не получен или пуст")
//        }
//    }
//
//    private fun getAccessToken(): String {
//        // Реализация получения токена из хранилища
//        // Например, через AuthDataStore
//        return AuthDataStore.getAccessToken()
//    }
//
//    suspend fun searchUsers(username: String): List<UserSearchDto> {
//        val response = client.get("/users/search") {
//            header(HttpHeaders.Authorization, authHeader())
//            parameter("username", username)
//        }
//        return when (response.status) {
//            HttpStatusCode.OK -> response.body()
//            else -> throw Exception("Ошибка поиска: ${response.status}")
//        }
//    }
//
//    suspend fun getFriends(): List<UserSearchDto> {
//        val response = client.get("/users/friends") {
//            header(HttpHeaders.Authorization, authHeader())
//        }
//        return when (response.status) {
//            HttpStatusCode.OK -> response.body()
//            else -> throw Exception("Ошибка загрузки друзей: ${response.status}")
//        }
//    }
//
//    suspend fun sendFriendRequest(dto: FriendRequestDto) {
//        val response = client.post("/users/friends/request") {
//            header(HttpHeaders.Authorization, authHeader())
//            contentType(ContentType.Application.Json)
//            setBody(dto)
//        }
//        if (response.status != HttpStatusCode.OK) {
//            throw Exception("Ошибка отправки заявки: ${response.status}")
//        }
//    }
//
//    suspend fun getFriendRequests(): List<FriendRequest> {
//        val response = client.get("/users/friends/requests") {
//            header(HttpHeaders.Authorization, authHeader())
//        }
//        return when (response.status) {
//            HttpStatusCode.OK -> response.body()
//            else -> throw Exception("Ошибка загрузки заявок: ${response.status}")
//        }
//    }
//
//    suspend fun acceptFriendRequest(dto: AcceptFriendRequestDto) {
//        val response = client.post("/users/friends/accept") {
//            header(HttpHeaders.Authorization, authHeader())
//            contentType(ContentType.Application.Json)
//            setBody(dto)
//        }
//        if (response.status != HttpStatusCode.OK) {
//            throw Exception("Ошибка принятия заявки: ${response.status}")
//        }
//    }
//
//    suspend fun createChat(targetUserId: String) : String {
//        val response = client.post("/chats/direct") {
//            header(HttpHeaders.Authorization, authHeader())
//            contentType(ContentType.Application.Json)
//            setBody(CreateChatRequest(targetUserId))
//        }
//        return when (response.status) {
//            HttpStatusCode.Created -> response.body<CreateChatResponse>().chatId
//            else -> throw Exception("Ошибка создания чата: ${response.status}")
//        }
//    }
//
//
//    suspend fun getChats(): List<ChatDto> {
//        val response = client.get("/chats") {
//            header(HttpHeaders.Authorization, authHeader())
//        }
//        return when (response.status) {
//            HttpStatusCode.OK -> response.body()
//            else -> throw Exception("Ошибка загрузки чатов: ${response.status}")
//        }
//    }
//
//    suspend fun getMessages(chatId: String, limit: Int = 50, offset: Long = 0): List<MessageDto> {
//        val response = client.get("/chats/$chatId/messages") {
//            header(HttpHeaders.Authorization, authHeader())
//            parameter("limit", limit)
//            parameter("offset", offset)
//        }
//        return when (response.status) {
//            HttpStatusCode.OK -> response.body()
//            else -> throw Exception("Ошибка загрузки сообщений: ${response.status}")
//        }
//    }
//
//    suspend fun sendMessage(chatId: String, content: String, type: String = "text"): MessageDto {
//        val response = client.post("/chats/$chatId/messages") {
//            header(HttpHeaders.Authorization, authHeader())
//            contentType(ContentType.Application.Json)
//            setBody(SendMessageRequest(content, type))
//        }
//        return when (response.status) {
//            HttpStatusCode.Created -> response.body()
//            else -> throw Exception("Ошибка отправки: ${response.status}")
//        }
//    }
}