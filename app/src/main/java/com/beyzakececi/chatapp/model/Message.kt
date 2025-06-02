package com.beyzakececi.chatapp.model
import com.beyzakececi.chatapp.data.local.MessageEntity

data class Message(
    val text: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val timestamp: com.google.firebase.Timestamp? = null
){
    fun toEntity(firestoreId: String): MessageEntity {
        val tsMillis = timestamp?.toDate()?.time ?: 0L
        return MessageEntity(
            firestoreId = firestoreId,
            text = text,
            senderId = senderId,
            receiverId = receiverId,
            timestamp = tsMillis
        )
    }
}
