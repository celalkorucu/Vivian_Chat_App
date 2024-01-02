package com.celalkorucu.xxx.Manager

import com.celalkorucu.xxx.Model.User


class ToUserManager private constructor(){


    private var currentUser: User? = null

    fun setCurrentUser(user: User) {
        currentUser = user
    }

    fun getCurrentUser(): User? {
        return currentUser
    }

    companion object {
        @Volatile
        private var instance: ToUserManager? = null

        fun getInstance(): ToUserManager {
            return instance ?: synchronized(this) {
                instance ?: ToUserManager().also { instance = it }
            }
        }
    }
}