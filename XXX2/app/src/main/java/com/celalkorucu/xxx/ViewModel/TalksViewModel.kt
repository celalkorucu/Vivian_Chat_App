package com.celalkorucu.xxx.ViewModel

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.celalkorucu.xxx.Activitys.HTTP_REQUEST_BASE_URL
import com.celalkorucu.xxx.Helper.AddTalkStatus
import com.celalkorucu.xxx.Helper.LoginStatus
import com.celalkorucu.xxx.Manager.ConnectionManager
import com.celalkorucu.xxx.Manager.DatabaseManager
import com.celalkorucu.xxx.Manager.MyUserManager
import com.celalkorucu.xxx.Manager.ToUserManager
import com.celalkorucu.xxx.Model.Data
import com.celalkorucu.xxx.Model.Message
import com.celalkorucu.xxx.Model.ReadContactDto
import com.celalkorucu.xxx.Model.ResultItem
import com.celalkorucu.xxx.Model.Talk
import com.celalkorucu.xxx.Model.User
import com.celalkorucu.xxx.Service.ServiceAPI
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class TalksViewModel: ViewModel()  {


    //Manager'lar
    private var myUserManager : MyUserManager = MyUserManager.getInstance()
    private  var connectionManager : ConnectionManager = ConnectionManager.getInstance()
    private  var toUserManager : ToUserManager = ToUserManager.getInstance()
    private var databaseManager : DatabaseManager = DatabaseManager.getInstance()


    //Sohbetler listesinin dinlenmesi için değişken
    private val _talksList = MutableLiveData<List<Talk>>()
    val talksList: LiveData<List<Talk>> get() = _talksList

    //Çıkış işleminin dinlenmesi için değişken
    private val _loginStatus = MutableLiveData<LoginStatus>()
    val loginStatus : LiveData<LoginStatus> get() = _loginStatus

    //Yeni sohbet ekranının dinlenmesi için değişken
    private val _addTalkStatus = MutableLiveData<AddTalkStatus>()
    val addTalkStatus : LiveData<AddTalkStatus> get() = _addTalkStatus


    //init
    init {
        getTalks(myUserManager.getCurrentUser()!!.username)
    }


    //Sohbetleri mesajlar tablosundan getiren metod
    @SuppressLint("Range")
    fun getTalks(myUsername : String){

        //var ListTalk = ArrayList<Talk>()
        var chatPartnerList = ArrayList<String>()

        databaseManager.getCurrentDatabase()?.let {

            try {

                //En az bir mesajımızın olduğu kişileri veren SQL sorgusu
                val cursor = it.rawQuery("SELECT DISTINCT CASE WHEN sender_username = '$myUsername' THEN receiver_username ELSE sender_username END AS chat_partner FROM messages WHERE sender_username = '$myUsername' OR receiver_username = '$myUsername'",null)

                while (cursor.moveToNext()) {
                    val chatPartner = cursor.getString(cursor.getColumnIndex("chat_partner"))
                    chatPartnerList.add(chatPartner)
                }

                //Verdiği kullanıcı isimlerini talk nesnesine çevirir
                readContactTalks(chatPartnerList)

            }catch (e : Exception){
                e.printStackTrace()
            }
        }
    }

    //Verilen kullanıcı adlarını sunucu yardımıyla Talk nesnesine çevirir
    private fun readContactTalks(chatPartners : ArrayList<String>){

        var listtalk = ArrayList<Talk>()
        for (chatPartner in chatPartners){

            val apiService = Retrofit.Builder()
                .baseUrl(HTTP_REQUEST_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ServiceAPI::class.java)
            val readContactDto = ReadContactDto(chatPartner)
            val call = apiService.readContact(readContactDto)


            call.enqueue(object : Callback<ResultItem> {

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<ResultItem>, response: Response<ResultItem>) {

                    val resultItem = response.body()

                    resultItem.let {

                        val data : Data = it!!.data  as Data

                        val profilePhotoBitmap = string64ToBitmap(data.image)
                        val talkUser = User(profilePhotoBitmap,chatPartner)

                        //Verilen kişi ile olan son mesajı getiren metod
                        val lastMessage = getLastMessage(chatPartner)


                        lastMessage?.let {
                            val talk = Talk(talkUser,lastMessage)
                            listtalk.add(talk)
                            _talksList.postValue(listtalk)

                        }?: println("LastMessage null")
                    }
                }
                override fun onFailure(call: Call<ResultItem>, t: Throwable) {
                    t.printStackTrace()
                    _loginStatus.postValue(LoginStatus.ERROR)
                }
            })
        }
    }

    //Verilen kullanıcı adını kullanarak database'den son mesajı getirir
    @SuppressLint("Range", "Recycle")
    fun getLastMessage(otherUsername : String) : Message?{

        databaseManager.getCurrentDatabase()?.let {
            try {
                val myUsername = myUserManager.getCurrentUser()!!.username

                //Son mesajı getiren SQL sorgusu
                val cursor = it.rawQuery("SELECT * FROM messages WHERE (sender_username = '$myUsername' AND receiver_username = '$otherUsername') OR (sender_username = '$otherUsername' AND receiver_username = '$myUsername') ORDER BY datetime(timestamp) DESC LIMIT 1",null)

                if (cursor.moveToFirst()) {
                    val messageId = cursor.getInt(cursor.getColumnIndex("id"))
                    val senderUsername = cursor.getString(cursor.getColumnIndex("sender_username"))
                    val receiverUsername = cursor.getString(cursor.getColumnIndex("receiver_username"))
                    val message = cursor.getString(cursor.getColumnIndex("message_text"))
                    val time = cursor.getString(cursor.getColumnIndex("timestamp"))


                    //Getirilen son mesaj nesnesi
                    val lastMessage  = Message(senderUsername,receiverUsername,message,time)
                    println(lastMessage.messageText)

                    lastMessage?.let {
                        println(it.messageText)
                    }?: println("Null")
                    return  lastMessage
                }
            }catch (e : Exception){
                e.printStackTrace()
                println("Son mesajı getiren sorguda hata meydana geldi")
            }
        }
        return Message("celalkorucu" ,"Daenerys", "100","2023-01-01 12:34:56")
    }


    //Yeni sohbet için toUser nesnesini oluşturan metod
    fun addTalk(name : String){
        val apiService = Retrofit.Builder()
            .baseUrl(HTTP_REQUEST_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServiceAPI::class.java)
        val readContactDto = ReadContactDto(name)
        val call = apiService.readContact(readContactDto)


        call.enqueue(object : Callback<ResultItem> {
            override fun onResponse(call: Call<ResultItem>, response: Response<ResultItem>) {

                val resultItem = response.body()

                resultItem.let {resultItem->


                    resultItem!!.data.let {data ->
                        val data : Data = data  as Data
                        val profilePhotoBitmap = string64ToBitmap(data.image)
                        val toUser = User(profilePhotoBitmap,name)

                        toUserManager.setCurrentUser(toUser)
                        _addTalkStatus.postValue(AddTalkStatus.SUCCESS)
                    }
                }
            }
            override fun onFailure(call: Call<ResultItem>, t: Throwable) {
                t.printStackTrace()
                _addTalkStatus.postValue(AddTalkStatus.ERROR)
                println("Add talk post işleminde hata meydana geldi")
            }
        })
    }

    //String bir biçimde verilen profil fotoğrafını bitmap türüne çeviren metod
    fun string64ToBitmap(string64Image: String): Bitmap {
        val byteArray = Base64.decode(string64Image, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    //SignOut işlemi
    fun signOut(){
        /*
        -> Socket bağlantısı kapatılır
        */
        _loginStatus.value = LoginStatus.CLOSE
        connectionManager.getCurrentConnection()!!.close()
    }
}