package com.wiwchat.features.chat.data.mapper

import com.wiwchat.features.chat.presentation.model.*
import com.wiwchat.shared.features.chat.domain.model.*

//fun Message.toUi(currentUserId: String): MessageUiModel {
//    val text = when (val content = this.content) {
//        is MessageContent.Text -> content.text
//        is MessageContent.Voice -> "[Голосовое \${content.durationMs / 1000} с]"
//        else -> "[Другое]"
//    }
//
//    val isSelf = this.senderId == currentUserId
//    val formattedTime = this.sentAt.toString()
//
//    return MessageUiModel(
//        id = this.id,
//        text = text,
//        isSelf = isSelf,
//        time = formattedTime,
//        senderName = this.senderName,
//        senderAvatar = this.senderAvatar,
//        status = when {
//            this.isRead -> MessageStatus.Read
//            this.isSent -> MessageStatus.Sent
//            this.isError -> MessageStatus.Error
//            else -> MessageStatus.Sending
//        }
//    )
//}