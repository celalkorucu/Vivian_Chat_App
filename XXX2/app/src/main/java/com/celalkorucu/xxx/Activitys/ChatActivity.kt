package com.celalkorucu.xxx.Activitys

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.celalkorucu.xxx.Adapter.MessageAdapter
import com.celalkorucu.xxx.Adapter.TalksAdapter
import com.celalkorucu.xxx.Manager.ConnectionManager
import com.celalkorucu.xxx.Manager.DatabaseManager
import com.celalkorucu.xxx.Manager.MyUserManager
import com.celalkorucu.xxx.Manager.ToUserManager
import com.celalkorucu.xxx.Model.Message
import com.celalkorucu.xxx.Model.User
import com.celalkorucu.xxx.ViewModel.ChatViewModel
import com.celalkorucu.xxx.ViewModel.TalksViewModel
import com.celalkorucu.xxx.databinding.ActivityChatBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatActivity : AppCompatActivity() {


    private lateinit var myUser : User
    private lateinit var toUser : User

    private lateinit var adapter : MessageAdapter
    private lateinit var chatViewModel: ChatViewModel
    private  var myUserManager: MyUserManager = MyUserManager.getInstance()
    private  var toUserManager: ToUserManager = ToUserManager.getInstance()
    private var databaseManager : DatabaseManager = DatabaseManager.getInstance()
    private  var connectionManager: ConnectionManager = ConnectionManager.getInstance()

    private var messageList = ArrayList<Message>()
    private lateinit var binding: ActivityChatBinding


    @SuppressLint("NotifyDataSetChanged")
    //onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)


        myUser  = myUserManager.getCurrentUser()!!
        toUser = toUserManager.getCurrentUser()!!

        binding.chatUserName.text = toUser.username
        binding.chatUserProfilePhoto.setImageBitmap(toUser.profilePhoto)


        //VIEW MODEL SETTINGS

        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]



        //Mesajlaşılan kişi ile olan mesajların dinlenme anı
        chatViewModel.messages!!.observe(this){
            messageList.clear()
            messageList.addAll(it)
            adapter = MessageAdapter(myUser,messageList)
            binding.chatRecyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
            adapter.let {
                binding.chatRecyclerView.scrollToPosition(adapter.getItemCount() - 1);

            }
        }

        //Mesajlaşılan kişiden gelen mesajların dinlenme anı
        hubConnectionViewModel.inComingMessage.observe(this){
            chatViewModel.getSelectedMessage()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    //Send Message Button Click
    fun sendMessageClick(view : View){

        val messageToSendString = binding.chatEditText.text.toString()
        if(messageToSendString.isNotEmpty()){

            binding.chatEditText.text.clear()

            //Mesajın Server'a gönderilmesini sağlayan kod
            try {
                connectionManager.getCurrentConnection()!!.send("SendMessage",messageToSendString,toUser.username)

                val currentLocalDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val timeStamp = currentLocalDateTime.format(formatter)
                val lastMessage = Message(myUserManager.getCurrentUser()!!.username , toUser.username , messageToSendString,timeStamp)

                addDatabaseMessage(lastMessage)
                chatViewModel.getSelectedMessage()
                talksViewModel.getTalks(myUser.username)
                binding.chatRecyclerView.scrollToPosition(adapter.getItemCount() - 1);


            }catch (e : Exception){
                e.printStackTrace()
                Toast.makeText(this@ChatActivity , "Mesaj gönderilirken hata meydana geldi" , Toast.LENGTH_LONG).show()
            }
        }
    }

    //Verilen mesaj nesnesini veri tabanına ekleyen metod
    private fun addDatabaseMessage(message : Message){


        val senderName = message.senderUser
        val receiverName = message.receivedUser
        val messageText = message.messageText
        val timestamp = message.timeStamp

        databaseManager.getCurrentDatabase()?.let {

            try {
                it.execSQL("INSERT INTO messages (sender_username,receiver_username,message_text,timestamp) VALUES ('$senderName','$receiverName','$messageText','$timestamp')")

            }catch (e : Exception){
                e.printStackTrace()
                Toast.makeText(this@ChatActivity , "Mesaj veri tabanına eklenirken hata meydana geldi",Toast.LENGTH_LONG).show()
            }
        }
    }
}
