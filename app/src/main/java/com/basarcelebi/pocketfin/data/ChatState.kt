package com.basarcelebi.pocketfin.data

import android.graphics.Bitmap

data class ChatState(
    val chatList: MutableList<Chat> = mutableListOf(),
    val prompt: String = "",
    val bitmap: Bitmap? = null
)
