package com.beyzakececi.chatapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.beyzakececi.chatapp.databinding.ActivityMainBinding
import com.beyzakececi.chatapp.model.Message
import com.beyzakececi.chatapp.ui.ChatAdapter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ChatAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val messages = mutableListOf<Message>()
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 0) Oturum kontrolü
        if (!Prefs.isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = auth.currentUser?.uid ?: ""

        // 1) RecyclerView + Adapter
        adapter = ChatAdapter(messages)
        binding.rvMessages.layoutManager = LinearLayoutManager(this)
        binding.rvMessages.adapter = adapter

        // 2) Firestore’dan mesaj dinleme (manuel okuma, toObject() yok)
        db.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(
                        this,
                        "Mesaj dinleme hatası: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                snapshots?.documentChanges?.forEach { dc ->
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val doc = dc.document

                        // a) text, senderId, receiverId alanlarını oku
                        val text = doc.getString("text") ?: ""
                        val senderId = doc.getString("senderId") ?: ""
                        val receiverId = doc.getString("receiverId") ?: ""

                        // b) timestamp alanını önce raw olarak al
                        val rawTimestamp = doc.get("timestamp")
                        val ts: Timestamp? = when (rawTimestamp) {
                            is Timestamp -> {
                                // Yeni mesajlardan gelen Firebase Timestamp
                                rawTimestamp
                            }
                            is Long -> {
                                // Eski mesajlardan gelen epoch ms (Long) ise bunu Timestamp’a dönüştür
                                val seconds = rawTimestamp / 1000
                                val nanos = ((rawTimestamp % 1000) * 1_000_000).toInt()
                                Timestamp(seconds, nanos)
                            }
                            else -> {
                                // Eğer alan yoksa veya farklı tipse null bırak
                                null
                            }
                        }

                        // c) Message nesnesini oluştur
                        val message = Message(
                            text = text,
                            senderId = senderId,
                            receiverId = receiverId,
                            timestamp = ts
                        )

                        // d) Adapter’a ekle ve scroll en alta kaydır
                        adapter.addMessage(message)
                        binding.rvMessages.scrollToPosition(messages.size - 1)
                    }
                }
            }

        // 3) Mesaj gönderme: FieldValue.serverTimestamp() ile Firestore’a ekle
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            val messageMap = hashMapOf<String, Any>(
                "text" to text,
                "senderId" to currentUserId,
                "receiverId" to "",
                "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("messages")
                .add(messageMap)
                .addOnSuccessListener {
                    binding.etMessage.text?.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Mesaj gönderilemedi: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        // 4) Kullanıcılar ekranına geçiş
        binding.btnUserList.setOnClickListener {
            startActivity(Intent(this, UserListActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                Prefs.isLoggedIn = false
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        Prefs.lastSeenTimestamp = System.currentTimeMillis()
    }
}
