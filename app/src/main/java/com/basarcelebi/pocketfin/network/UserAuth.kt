package com.basarcelebi.pocketfin.network

import com.basarcelebi.pocketfin.database.User
import com.google.firebase.auth.FirebaseAuth

class UserAuth {
    var user: User? = null
        private set

    fun fetchUserData() {
        // Örneğin, Firebase'den kullanıcı verilerini alıyor olabilirsiniz
        FirebaseAuth.getInstance().currentUser?.let { firebaseUser ->
            user = User(
                id = firebaseUser.uid,
                displayName = firebaseUser.displayName,
                email = firebaseUser.email,
                profileImageUrl = firebaseUser.photoUrl?.toString() // Profil resim URL'sini buradan alın
            )
        }
    }

    companion object {
        fun logout() {
            FirebaseAuth.getInstance().signOut()
        }
    }
}
