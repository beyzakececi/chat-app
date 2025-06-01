package com.beyzakececi.chatapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnSignUp: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Eğer daha önce oturum açılmışsa doğrudan MainActivity'ye geç
        if (Prefs.isLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        etEmail    = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin   = findViewById(R.id.btnLogin)
        btnSignUp  = findViewById(R.id.btnSignUp)

        auth = FirebaseAuth.getInstance()

        btnSignUp.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass  = etPassword.text.toString().trim()
            if (email.isEmpty() || pass.length < 6) {
                Toast.makeText(this, "Geçerli e-posta ve en az 6 haneli şifre girin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1) Firebase Authentication'da kullanıcı oluştur
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // 2) Oluşan kullanıcının UID'sini al
                        val currentUser = auth.currentUser
                        if (currentUser == null) {
                            Toast.makeText(this, "Beklenmedik: Kullanıcı alınamadı", Toast.LENGTH_LONG).show()
                            return@addOnCompleteListener
                        }
                        val uid = currentUser.uid

                        // 3) Firestore'a kaydedilecek kullanıcı bilgisi
                        val userMap = hashMapOf(
                            "uid" to uid,
                            "email" to email
                        )

                        // 4) Firestore 'users' koleksiyonuna ekleme
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                // Yazma başarılıysa oturumu saklayıp MainActivity'ye geç
                                Prefs.isLoggedIn = true
                                Toast.makeText(this, "Kayıt ve Firestore kaydı başarılı!", Toast.LENGTH_SHORT).show()
                                goToMainActivity()
                            }
                            .addOnFailureListener { e ->
                                // Yazma sırasında bir hata varsa, kullanıcıya bilgi ver
                                Toast.makeText(this, "Firestore yazma hatası: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(this, "Kayıt hatası: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass  = etPassword.text.toString().trim()
            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Oturumu sakla ve MainActivity'ye geç
                        Prefs.isLoggedIn = true
                        goToMainActivity()
                    } else {
                        Toast.makeText(this, "Giriş hatası: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
