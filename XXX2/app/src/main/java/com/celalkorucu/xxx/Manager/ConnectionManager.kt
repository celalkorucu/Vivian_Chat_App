package com.celalkorucu.xxx.Manager

import com.microsoft.signalr.HubConnection

class ConnectionManager private constructor(){


    private var hubConnection: HubConnection? = null

    fun setCurrentConnection(hubConnection: HubConnection) {
        this.hubConnection = hubConnection
    }

    fun getCurrentConnection(): HubConnection? {
        return hubConnection
    }

    companion object {
        @Volatile
        private var instance: ConnectionManager? = null

        fun getInstance(): ConnectionManager {
            return instance ?: synchronized(this) {
                instance ?: ConnectionManager().also { instance = it }
            }
        }
    }
}