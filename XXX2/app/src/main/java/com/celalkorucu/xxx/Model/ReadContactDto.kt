package com.celalkorucu.xxx.Model

import com.google.gson.annotations.SerializedName

data class ReadContactDto(
    @SerializedName("name")
    val username: String
)
