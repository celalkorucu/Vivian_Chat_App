package com.celalkorucu.xxx.Activitys


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.celalkorucu.xxx.Adapter.TalksAdapter
import com.celalkorucu.xxx.Helper.AddTalkStatus
import com.celalkorucu.xxx.Helper.DialogHelper
import com.celalkorucu.xxx.Helper.LoginStatus
import com.celalkorucu.xxx.Manager.DatabaseManager
import com.celalkorucu.xxx.Manager.MyUserManager
import com.celalkorucu.xxx.Manager.ToUserManager
import com.celalkorucu.xxx.Model.Talk
import com.celalkorucu.xxx.Model.User
import com.celalkorucu.xxx.R
import com.celalkorucu.xxx.ViewModel.TalksViewModel
import com.celalkorucu.xxx.databinding.ActivityTalksBinding

lateinit var talksViewModel: TalksViewModel

class TalksActivity : AppCompatActivity() , NewChatFragment.AddTalkListener{

    private  var myUserManager: MyUserManager = MyUserManager.getInstance()
    private  var toUserManager: ToUserManager = ToUserManager.getInstance()
    private var databaseManager : DatabaseManager = DatabaseManager.getInstance()

    private lateinit var binding :ActivityTalksBinding
    private lateinit var myUser : User
    private lateinit var talksList: ArrayList<Talk>
    private lateinit var adapter : TalksAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTalksBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        talksList = ArrayList()


        //SQLite Database initialize
        val vivianDatabase = this.openOrCreateDatabase("Vivian" , MODE_PRIVATE , null)
        databaseManager.setCurrentDatabase(vivianDatabase)



        //SQLite mesajlar için database yoksa oluşturan kod
        vivianDatabase.execSQL("CREATE TABLE IF NOT EXISTS messages (" +
                "id INTEGER PRIMARY KEY ," +
                " sender_username VARCHAR ," +
                " receiver_username VARCHAR ," +
                " message_text VARCHAR ," +
                " timestamp TEXT )"
        )

        //User manager settings
        myUser = myUserManager.getCurrentUser()!!


        //RecyclerView Settings
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TalksAdapter(myUser,talksList)
        binding.recyclerView.adapter=adapter

        //Binding Settings
        binding.myTalksProfilePhoto.setImageBitmap(myUser.profilePhoto)
        binding.myTalksUserName.text = myUser.username

        //talksViewModel initialize
        talksViewModel = ViewModelProvider(this)[TalksViewModel::class.java]

        //Sohbet ettiğimiz kişilerin dinlenme anı
        talksViewModel.talksList.observe(this){

            adapter = TalksAdapter(myUser,it)
            binding.recyclerView.adapter=adapter
            adapter.notifyDataSetChanged()
        }

        //Gelen mesajların dinlenme anı
        hubConnectionViewModel.inComingMessage.observe(this){

            //Veri tabanına eklenen mesajları gönderen ve alan kişilere göre sohbet nesnesi oluşturur ve bunu listeye eşitler
            talksViewModel.getTalks(myUser.username)
        }

        //Login statusu'n dinlenme anı
        talksViewModel.loginStatus.observe(this){
            if(it == LoginStatus.CLOSE){
                val intent = Intent(this@TalksActivity , LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        //Yeni sohbet oluşturulasının dinlenme anı
        talksViewModel.addTalkStatus.observe(this){addTalkStatus->
            if (addTalkStatus == AddTalkStatus.SUCCESS){
                val intent = Intent(this@TalksActivity , ChatActivity::class.java)
                startActivity(intent)
            }else if (addTalkStatus == AddTalkStatus.USER_NOT_FOUND){
                Toast.makeText(this@TalksActivity , "Kullanıcı bulunamadı",Toast.LENGTH_LONG).show()
            }else {
                Toast.makeText(this@TalksActivity , "Chat ekranına bağlanırken hata meydana geldi",Toast.LENGTH_LONG).show()
            }
        }
    }

    //AddTalkClick
    fun addTalkClick(view : View){
        val dialog = NewChatFragment()
        dialog.show(supportFragmentManager, "AddTalkListener")
    }

    //SignOut Click
    fun signOutClick(view : View){
        //Oturum sonlanma işlemi gerçekleştirilecek
        showAlertDialog()
    }

    //Oturumu sonlandıran Alert Dialog
    private fun showAlertDialog(){

        val alertDialog = DialogHelper.alertBuilder(this@TalksActivity)
        alertDialog.setTitle("Çıkış Yap")
        alertDialog.setMessage("Oturumu sonlandırmak istediğinize emin misiniz ?")

        alertDialog.setPositiveButton("Evet"){ dialog , which ->

            talksViewModel.signOut()
            dialog.dismiss()
        }

        alertDialog.setNegativeButton("Hayır",null)
        alertDialog.setIcon(R.drawable.sign_out)
        alertDialog.show()
    }

    //Add talk işlemi gerçekleşirse
    override fun okAddTalk(name: String) {
        talksViewModel.addTalk(name)
    }

}