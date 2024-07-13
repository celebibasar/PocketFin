package com.basarcelebi.pocketfin.network

import com.google.firebase.auth.FirebaseAuth

class UserAuth {
    private val auth = FirebaseAuth.getInstance()

    val user = auth.currentUser

    fun logout() {
        auth.signOut()
    }

    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }
}