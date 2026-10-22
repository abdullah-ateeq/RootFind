package com.example.rootfind.model

data class RootFindHistory(
    val id: String,
    val rootType: RootType,
    val value: String,
    val rootIndex: String,
    val savedAt: Long
)
