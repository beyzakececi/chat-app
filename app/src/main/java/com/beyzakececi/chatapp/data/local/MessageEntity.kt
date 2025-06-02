package com.beyzakececi.chatapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0, // Room için kendi PK
    val firestoreId: String,   // Firestore doküman ID’si
    val text: String,
    val senderId: String,
    val receiverId: String,
    val timestamp: Long        // epoch millis
)
