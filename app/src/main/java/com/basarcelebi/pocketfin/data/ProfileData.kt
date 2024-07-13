package com.basarcelebi.pocketfin.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import com.basarcelebi.pocketfin.model.ProfileOptions

object ProfileData {
    val defaultData = ProfileOptionsData()[0]

    fun ProfileOptionsData():List<ProfileOptions>
    {
        return listOf(
            ProfileOptions(
                name = "Account",
                description = "You can see the detail of your account",
                icon = Icons.Default.AccountCircle

            ),
            ProfileOptions(
                name = "Security",
                description = "You can read the privacy policy of the PocketFin App",
                icon = Icons.Default.Lock

            ),
            ProfileOptions(
                name = "About",
                description = "You can review the information about the PocketFin App",
                icon = Icons.Default.Info

            ),
            ProfileOptions(
                name = "Log Out",
                description = "You can log out of the PocketFin App",
                icon = Icons.Default.Close

            )

        )

    }
}