package com.example.umc_flo

import androidx.room.Entity
import androidx.room.PrimaryKey


data class Album(
    var title: String? = "",
    var singer: String? = "",
    var coverImg: Int? = null,
    var songs: ArrayList<Song>? = null
)
