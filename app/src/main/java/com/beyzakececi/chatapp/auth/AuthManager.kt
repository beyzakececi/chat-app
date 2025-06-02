package com.beyzakececi.chatapp.auth

import com.google.firebase.auth.FirebaseAuth

object AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Oturum Açmış Kullanıcının ID Token’ını Alır.
     * callback: ID Token string’i veya null
     */
    fun fetchIdToken(forceRefresh: Boolean = false, callback: (String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            callback(null)
            return
        }
        user.getIdToken(forceRefresh)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(task.result?.token)
                } else {
                    callback(null)
                }
            }
    }
}
