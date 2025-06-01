package com.beyzakececi.chatapp.model
import com.google.firebase.Timestamp

data class Message(
    val text: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val timestamp: com.google.firebase.Timestamp? = null
)
