package com.celalkorucu.xxx.ViewModel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.celalkorucu.xxx.Activitys.HTTP_REQUEST_BASE_URL
import com.celalkorucu.xxx.Manager.ConnectionManager
import com.celalkorucu.xxx.Manager.DatabaseManager
import com.celalkorucu.xxx.Manager.MyUserManager
import com.celalkorucu.xxx.Manager.ToUserManager
import com.celalkorucu.xxx.Model.Data
import com.celalkorucu.xxx.Model.Message
import com.celalkorucu.xxx.Model.ReadContactDto
import com.celalkorucu.xxx.Model.ResultItem
import com.celalkorucu.xxx.Model.User
import com.celalkorucu.xxx.Service.ServiceAPI
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatViewModel : ViewModel() {

    //Manager'lar
    private val connectionManager : ConnectionManager = ConnectionManager.getInstance()
    private val toUserManager : ToUserManager = ToUserManager.getInstance()
    private val myUserManager : MyUserManager = MyUserManager.getInstance()
    private  var databaseManager : DatabaseManager = DatabaseManager.getInstance()


    //Mesajlaşılan kişi ile olan mesajları dinleyen değişken
    private val _messages = MutableLiveData<ArrayList<Message>>()
    val messages : LiveData<ArrayList<Message>>? get() = _messages

    init {
        getSelectedMessage()
    }

    //Mesajlaşılan kişi ile olan bütün mesajları listeye aktaran metod metod
    fun getSelectedMessage(){

         val messageList = ArrayList<Message>()

        val myUser = myUserManager.getCurrentUser()
        val toUser = toUserManager.getCurrentUser()
        databaseManager.getCurrentDatabase()?.let {

            val cursor = it.rawQuery("SELECT * FROM messages",null)

            val idIx = cursor.getColumnIndex("id")
            val senderUsernameIx = cursor.getColumnIndex("sender_username")
            val receiverNameIx = cursor.getColumnIndex("receiver_username")
            val messageTextIx = cursor.getColumnIndex("message_text")
            val timestampIx = cursor.getColumnIndex("timestamp")

            while (cursor.moveToNext()){

                val senderUsername = cursor.getString(senderUsernameIx)
                val id = cursor.getInt(idIx)
                val receiverUsername = cursor.getString(receiverNameIx)
                val messageText = cursor.getString(messageTextIx)
                val timestamp = cursor.getString(timestampIx)


                myUser?.let {
                    toUser?.let {
                        //Ya benin gönderici onun alıcı ya da onun gönderici benim alıcı oldugum mesajları almamızı sağlayan ifade
                        if ((senderUsername == myUser!!.username && receiverUsername == toUser!!.username) ||
                            (senderUsername == toUser!!.username && receiverUsername == myUser!!.username))
                        {
                            val message = Message(senderUsername ,receiverUsername,messageText,timestamp)
                            messageList.add(message)
                        }
                    }
                }
            }
            _messages.postValue(messageList)
            cursor.close()
        }
    }
}