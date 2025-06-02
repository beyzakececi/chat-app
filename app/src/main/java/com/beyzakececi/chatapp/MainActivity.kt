package com.beyzakececi.chatapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.beyzakececi.chatapp.auth.AuthManager
import com.beyzakececi.chatapp.data.local.AppDatabase
import com.beyzakececi.chatapp.data.local.MessageEntity
import com.beyzakececi.chatapp.databinding.ActivityMainBinding
import com.beyzakececi.chatapp.model.Message
import com.beyzakececi.chatapp.network.FirestoreService
import com.beyzakececi.chatapp.network.RetrofitClient
import com.beyzakececi.chatapp.ui.ChatAdapter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ChatAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val messages = mutableListOf<Message>()
    private lateinit var currentUserId: String

    // Firestore REST için Retrofit servisi
    private val projectId = "chatappproject-3ae2c"
    private val firestoreService: FirestoreService = RetrofitClient.create()

    // Room database ve DAO
    private val messageDao by lazy { AppDatabase.getInstance(this).messageDao() }

    @SuppressLint("NotifyDataSetChanged")
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

        // 0a) Uygulama açıldığında önce Room’dan geçmiş mesajları yükle
        CoroutineScope(Dispatchers.IO).launch {
            val cached = messageDao.getAllMessages()
            cached.forEach { e ->
                val tsMillis = e.timestamp
                val firebaseTs = Timestamp(tsMillis / 1000, ((tsMillis % 1000) * 1_000_000).toInt())
                val msg = Message(
                    text = e.text,
                    senderId = e.senderId,
                    receiverId = e.receiverId,
                    timestamp = firebaseTs
                )
                messages.add(msg)
            }
            runOnUiThread {
                adapter.notifyDataSetChanged()
                binding.rvMessages.scrollToPosition(messages.size - 1)
            }
        }

        // 2) Firestore’dan mesaj dinleme (yeni mesaj geldiğinde hem UI’a ekle hem Room’a kaydet)
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

                snapshots?.documentChanges?.forEach { dc: DocumentChange ->
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val doc = dc.document

                        // a) text, senderId, receiverId alanlarını oku
                        val text = doc.getString("text") ?: ""
                        val senderId = doc.getString("senderId") ?: ""
                        val receiverId = doc.getString("receiverId") ?: ""

                        // b) timestamp alanını önce raw olarak al
                        val rawTimestamp = doc.get("timestamp")
                        val ts: Timestamp? = when (rawTimestamp) {
                            is Timestamp -> rawTimestamp
                            is Long -> {
                                val seconds = rawTimestamp / 1000
                                val nanos = ((rawTimestamp % 1000) * 1_000_000).toInt()
                                Timestamp(seconds, nanos)
                            }
                            else -> null
                        }

                        // c) Message nesnesini oluştur
                        val message = Message(
                            text = text,
                            senderId = senderId,
                            receiverId = receiverId,
                            timestamp = ts
                        )

                        // d) Adapter’a ekle ve scroll en alta kaydır
                        messages.add(message)
                        adapter.notifyItemInserted(messages.size - 1)
                        binding.rvMessages.scrollToPosition(messages.size - 1)

                        // e) Room’a kaydet
                        ts?.let {
                            val millis = it.toDate().time
                            val entity = MessageEntity(
                                firestoreId = doc.id,
                                text = text,
                                senderId = senderId,
                                receiverId = receiverId,
                                timestamp = millis
                            )
                            CoroutineScope(Dispatchers.IO).launch {
                                messageDao.insertMessage(entity)
                            }
                        }
                    }
                }
            }

        // 3) Mesaj gönderme: FieldValue.serverTimestamp() ile Firestore’a ekle ve Room’a da ekle
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
                .addOnSuccessListener { docRef ->
                    binding.etMessage.text?.clear()

                    // Anlık olarak Room’a yerel timestamp ile ekle
                    val nowMillis = System.currentTimeMillis()
                    val entity = MessageEntity(
                        firestoreId = docRef.id,
                        text = text,
                        senderId = currentUserId,
                        receiverId = "",
                        timestamp = nowMillis
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        messageDao.insertMessage(entity)
                    }
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

        // ——————————————————————————————————————————
        // 5) RESTful API Metotları burada. İsterseniz bir butona bağlayabilir veya
        //    uygulama açılışında bir kez çalıştırabilirsiniz.
        // ——————————————————————————————————————————

        // Örnek: uygulama açıldığında tüm kullanıcıları listele
        listAllUsers()
        // Örnek: yeni bir kullanıcı oluşturmak isterseniz
        // createNewUser("uniqueUserId123", "yenimail@example.com")
    }

    // ——————————————————————————————————————————
    // RESTful API Metodları (Firestore REST / Retrofit)
    // ——————————————————————————————————————————

    private fun listAllUsers() {
        AuthManager.fetchIdToken { token ->
            if (token == null) {
                runOnUiThread {
                    Toast.makeText(this, "Giriş yapmış kullanıcı yok!", Toast.LENGTH_SHORT).show()
                }
                return@fetchIdToken
            }
            val authHeader = "Bearer $token"

            firestoreService.listUsers(projectId, authHeader)
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        if (response.isSuccessful) {
                            val body = response.body()
                            Log.d("REST-ListUsers", "Users: $body")
                            // JSON içindeki "documents" dizisini parse edip ekrana ya da listeye basabilirsiniz.
                        } else {
                            Log.e("REST-ListUsers", "Error: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        Log.e("REST-ListUsers", "Network failure", t)
                    }
                })
        }
    }

    private fun createNewUser(userId: String, email: String) {
        AuthManager.fetchIdToken { token ->
            if (token == null) return@fetchIdToken
            val authHeader = "Bearer $token"

            // Firestore REST JSON gövdesi
            val newUserJson = JsonObject().apply {
                add("fields", JsonObject().apply {
                    add("uid", JsonObject().apply {
                        addProperty("stringValue", userId)
                    })
                    add("email", JsonObject().apply {
                        addProperty("stringValue", email)
                    })
                })
            }

            firestoreService.createUser(projectId, authHeader, newUserJson)
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        if (response.isSuccessful) {
                            Log.d("REST-CreateUser", "Created: ${response.body()}")
                        } else {
                            Log.e("REST-CreateUser", "Error: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        Log.e("REST-CreateUser", "Network failure", t)
                    }
                })
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
