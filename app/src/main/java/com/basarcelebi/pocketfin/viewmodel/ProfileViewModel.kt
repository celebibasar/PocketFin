package com.basarcelebi.pocketfin.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    val user = auth.currentUser

    fun logout() {
        auth.signOut()
    }

    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }
}
