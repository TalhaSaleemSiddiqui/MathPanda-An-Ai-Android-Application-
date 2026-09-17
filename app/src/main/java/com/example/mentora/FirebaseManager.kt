package com.example.mentora

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

object FirebaseManager {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // 1. Sign Up Function
    fun signUp(name: String, email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                        onResult(true, null)
                    }
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    // 2. Log In Function
    fun logIn(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onResult(true, null)
                else onResult(false, task.exception?.message)
            }
    }

    // 3. History Save Function
    fun saveHistory(question: String, answer: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val data = hashMapOf(
                "query" to question,
                "summary" to answer,
                "timestamp" to Date()
            )
            db.collection("users").document(currentUser.uid).collection("history").add(data)
                .addOnSuccessListener {
                    android.util.Log.d("FirebaseTest", "Success: The table has been created in the database!")
                }
                .addOnFailureListener { error ->
                    android.util.Log.e("FirebaseTest", "Failed: An error occurred in the database -> ${error.message}")
                }
        } else {
            android.util.Log.e("FirebaseTest", "Failed: User is null (not logged in)")
        }
    }

    // 4. History Fetch Function
    fun getHistory(onResult: (List<Map<String, Any>>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {

            db.collection("users").document(currentUser.uid).collection("history")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    val historyList = mutableListOf<Map<String, Any>>()
                    for (document in result) {
                        historyList.add(document.data)
                    }
                    onResult(historyList)
                }
                .addOnFailureListener {
                    onResult(emptyList())
                }
        } else {
            onResult(emptyList())
        }
    }
}