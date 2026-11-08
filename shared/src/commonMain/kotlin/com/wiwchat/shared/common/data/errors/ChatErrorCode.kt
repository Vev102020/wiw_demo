package com.wiwchat.shared.common.data.errors

enum class ChatErrorCode : ApiErrorCode {

    ;
    companion object {
        fun find(name: String): ApiErrorCode? = entries.find { it.name == name }
    }
}