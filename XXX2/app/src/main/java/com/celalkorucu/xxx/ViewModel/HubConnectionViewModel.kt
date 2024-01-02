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
import com.celalkorucu.xxx.Activitys.WS_URL
import com.celalkorucu.xxx.Helper.LoginStatus
import com.celalkorucu.xxx.Manager.ConnectionManager
import com.celalkorucu.xxx.Manager.DatabaseManager
import com.celalkorucu.xxx.Manager.MyUserManager
import com.celalkorucu.xxx.Model.Data
import com.celalkorucu.xxx.Model.Message
import com.celalkorucu.xxx.Model.ReadContactDto
import com.celalkorucu.xxx.Model.ResultItem
import com.celalkorucu.xxx.Model.User
import com.celalkorucu.xxx.Service.ServiceAPI
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HubConnectionViewModel : ViewModel() {

    private lateinit var globalUsername : String
    private lateinit var connection : HubConnection
    private var connectionManager: ConnectionManager = ConnectionManager.getInstance()
    private var databaseManager : DatabaseManager = DatabaseManager.getInstance()
    private var myUserManager : MyUserManager = MyUserManager.getInstance()

    private lateinit var senderUser : User


    //Server'a bağlı olup olmadığımızı dinleyen değişken
    private val _connectionStatus = MutableLiveData<LoginStatus>()
    val connectionStatus : LiveData<LoginStatus> get() = _connectionStatus

    //Gelen mesajları dinleyen değişken
    private val _inComingMessage = MutableLiveData<Message>()
    val inComingMessage : LiveData<Message> get() = _inComingMessage

    //Server bağlantımızı açan metod
    fun connectionServer(username : String){
        connection = HubConnectionBuilder
            .create(WS_URL)
            .withHeader("username" ,username)
            .build()

        globalUsername = username

        connection.start().blockingAwait()


        connectionManager.setCurrentConnection(connection)

        _connectionStatus.postValue(LoginStatus.SUCCESS)

        //Gelen mesajlar dinlenir ve veri tabanına eklenir
        listenMessages()
    }

    //Gelen mesajları dinleyen metod
    @OptIn(DelicateCoroutinesApi::class)
    private fun listenMessages(){
        connectionManager.getCurrentConnection()!!.on("ReceiveMessage",
            {
                    senderName,sendedMessage ->

                GlobalScope.launch {

                    //Verilen değerlere göre mesaj nesnesi oluşturulur
                    listenReadContact(senderName,sendedMessage)

                }

            },
            String::class.java,
            String::class.java
        )
    }

    //Mesaj nesnesini oluşturan metod
    private fun listenReadContact(senderName : String,sendedMessage : String){

        val apiService = Retrofit.Builder()
            .baseUrl(HTTP_REQUEST_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServiceAPI::class.java)
        val readContactDto = ReadContactDto(senderName)
        val call = apiService.readContact(readContactDto)


        call.enqueue(object : Callback<ResultItem> {

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<ResultItem>, response: Response<ResultItem>) {

                val resultItem = response.body()

                resultItem.let {

                    val data : Data = it!!.data  as Data

                    //Verilen string bitmap'e dönüşür
                    val profilePhotoBitmap = string64ToBitmap(data.image)

                    //senderUser = Mesajı gönderen kullanıcı
                    senderUser = User(profilePhotoBitmap,senderName)

                    val currentLocalDateTime = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val timeStamp = currentLocalDateTime.format(formatter)

                    val message = Message(senderUser.username ,myUserManager.getCurrentUser()!!.username , sendedMessage ,timeStamp)

                    //Gelen mesajın değişkene aktarılma anı
                    _inComingMessage.postValue(message)

                    //Gelen bütün mesajlar veri tabanına eklenir
                    addDatabaseMessage(message)
                }
            }
            override fun onFailure(call: Call<ResultItem>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    //Verilen mesaj nesnesini veri tabanına ekleyen metod
    private fun addDatabaseMessage(message : Message){

        val senderName = message.senderUser
        val receiverName = message.receivedUser
        val messageText = message.messageText
        val timestamp = message.timeStamp

        databaseManager.getCurrentDatabase()?.let {

            //Mesajlar tablosuna nesnemizi ekleyen SQL sorgusu
            it.execSQL("INSERT INTO messages (sender_username,receiver_username,message_text,timestamp) VALUES ('$senderName','$receiverName','$messageText','$timestamp')")
        }
    }

    //String bir biçimde verilen fotoğrafı bitmap'e dönüştüren metod
    fun string64ToBitmap(string64Image: String): Bitmap {
        val byteArray = Base64.decode(string64Image, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
}