package com.example.xbjsb.data.security

object PrivateAccessManager {
    private var isUnlocked = false

    fun unlock() {
        isUnlocked = true
    }

    fun lock() {
        isUnlocked = false
    }

    fun isUnlocked(): Boolean = isUnlocked
}
