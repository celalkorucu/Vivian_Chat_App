package com.celalkorucu.xxx.Manager
import com.celalkorucu.xxx.Model.User

class MyUserManager private constructor(){


    private var currentUser: User? = null

    fun setCurrentUser(user: User) {
        currentUser = user
    }

    fun getCurrentUser(): User? {
        return currentUser
    }

    companion object {
        @Volatile
        private var instance: MyUserManager? = null

        fun getInstance(): MyUserManager {
            return instance ?: synchronized(this) {
                instance ?: MyUserManager().also { instance = it }
            }
        }
    }
}