package com.wiwchat.shared.common.domain.errors

enum class ChatErrorCode : ApiErrorCode {

    ;
    companion object {
        fun find(name: String): ApiErrorCode? = entries.find { it.name == name }
    }
}