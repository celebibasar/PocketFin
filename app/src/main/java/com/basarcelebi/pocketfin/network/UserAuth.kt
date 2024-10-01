package com.basarcelebi.pocketfin.network

import com.google.firebase.auth.FirebaseAuth

class UserAuth {
    private val auth = FirebaseAuth.getInstance()

    val user = auth.currentUser

    companion object {
        fun logout() {
            FirebaseAuth.getInstance().signOut()
        }
    }

    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }
}