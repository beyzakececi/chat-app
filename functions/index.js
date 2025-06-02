const functions = require("firebase-functions");
const admin = require("firebase-admin");
const express = require("express");
const cors = require("cors");

// Firebase Admin SDK’yı başlat
admin.initializeApp();

// Firestore referansı
const db = admin.firestore();

// Express uygulamasını oluştur
const app = express();

// CORS’u tüm kaynaklara aç
app.use(cors({ origin: true }));
app.use(express.json()); // JSON gövdeli istekleri parse edecek

// Koleksiyon adı
const USERS_COLLECTION = "users";

// GET /users → Tüm kullanıcıları listele
app.get("/users", async (req, res) => {
  try {
    const snapshot = await db.collection(USERS_COLLECTION).get();
    const users = [];
    snapshot.forEach(doc => {
      users.push({ id: doc.id, ...doc.data() });
    });
    return res.status(200).json(users);
  } catch (error) {
    console.error("Error fetching users:", error);
    return res.status(500).json({ error: "Internal Server Error" });
  }
});

// GET /users/:id → Belirli bir kullanıcıyı getir
app.get("/users/:id", async (req, res) => {
  const userId = req.params.id;
  try {
    const doc = await db.collection(USERS_COLLECTION).doc(userId).get();
    if (!doc.exists) {
      return res.status(404).json({ error: "User not found" });
    }
    return res.status(200).json({ id: doc.id, ...doc.data() });
  } catch (error) {
    console.error("Error fetching user:", error);
    return res.status(500).json({ error: "Internal Server Error" });
  }
});

// POST /users → Yeni kullanıcı oluştur
app.post("/users", async (req, res) => {
  const { email, uid } = req.body;
  if (!email || !uid) {
    return res.status(400).json({ error: "email and uid are required" });
  }
  try {
    const userRef = db.collection(USERS_COLLECTION).doc(uid);
    await userRef.set({ email, uid });
    const newDoc = await userRef.get();
    return res.status(201).json({ id: newDoc.id, ...newDoc.data() });
  } catch (error) {
    console.error("Error creating user:", error);
    return res.status(500).json({ error: "Internal Server Error" });
  }
});

// PUT /users/:id → Varolan kullanıcıyı güncelle
app.put("/users/:id", async (req, res) => {
  const userId = req.params.id;
  const { email } = req.body;
  if (!email) {
    return res.status(400).json({ error: "email is required" });
  }
  try {
    const userRef = db.collection(USERS_COLLECTION).doc(userId);
    const doc = await userRef.get();
    if (!doc.exists) {
      return res.status(404).json({ error: "User not found" });
    }
    await userRef.update({ email });
    const updatedDoc = await userRef.get();
    return res.status(200).json({ id: updatedDoc.id, ...updatedDoc.data() });
  } catch (error) {
    console.error("Error updating user:", error);
    return res.status(500).json({ error: "Internal Server Error" });
  }
});

// DELETE /users/:id → Belirli bir kullanıcıyı sil
app.delete("/users/:id", async (req, res) => {
  const userId = req.params.id;
  try {
    const userRef = db.collection(USERS_COLLECTION).doc(userId);
    const doc = await userRef.get();
    if (!doc.exists) {
      return res.status(404).json({ error: "User not found" });
    }
    await userRef.delete();
    return res.status(200).json({ message: "User deleted successfully" });
  } catch (error) {
    console.error("Error deleting user:", error);
    return res.status(500).json({ error: "Internal Server Error" });
  }
});

// Express uygulamasını bir Cloud Function olarak expose et
exports.api = functions.https.onRequest(app);
