package com.beyzakececi.chatapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.beyzakececi.chatapp.ui.UserAdapter
import com.beyzakececi.chatapp.databinding.ActivityUserListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



class UserListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserListBinding
    private val userList = mutableListOf<User>()
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = UserAdapter(userList)
        binding.rvUserList.layoutManager = LinearLayoutManager(this)
        binding.rvUserList.adapter = adapter

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        FirebaseFirestore.getInstance()
            .collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                userList.clear()
                for (doc in snapshot.documents) {
                    val user = doc.toObject(User::class.java)
                    // Kendi hesabını listeye ekleme!
                    if (user != null && user.uid != currentUserId) {
                        userList.add(user)
                    }
                }
                adapter.notifyDataSetChanged()
            }
    }
}
