package com.example.xbjsb.data.security

object AppLockManager {
    private var isUnlocked = false
    private var lastActiveTime = 0L
    
    fun unlock() {
        isUnlocked = true
        lastActiveTime = System.currentTimeMillis()
    }
    
    fun lock() {
        isUnlocked = false
    }
    
    fun checkLockTimeout(timeoutMs: Long): Boolean {
        if (timeoutMs == 0L) return true
        val elapsed = System.currentTimeMillis() - lastActiveTime
        return elapsed > timeoutMs
    }
    
    fun updateActiveTime() {
        lastActiveTime = System.currentTimeMillis()
    }
    
    fun isLocked(): Boolean = !isUnlocked
}