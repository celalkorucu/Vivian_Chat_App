package com.celalkorucu.xxx.Activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.celalkorucu.xxx.Helper.LoginStatus
import com.celalkorucu.xxx.Manager.ConnectionManager
import com.celalkorucu.xxx.Manager.MyUserManager
import com.celalkorucu.xxx.Model.ResultItem
import com.celalkorucu.xxx.Model.ReadContactDto
import com.celalkorucu.xxx.Service.ServiceAPI
import com.celalkorucu.xxx.ViewModel.HubConnectionViewModel
import com.celalkorucu.xxx.ViewModel.LoginViewModel
import com.celalkorucu.xxx.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val IP_ADDRESS = "192.168.70.127"
const val HTTP_REQUEST_BASE_URL = "http://$IP_ADDRESS:5000/api/Contact/"
const val WS_URL = "http://$IP_ADDRESS:5000/chatHub"
lateinit var hubConnectionViewModel: HubConnectionViewModel


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var myUserManager: MyUserManager
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var connectionManager: ConnectionManager

    //onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)



        myUserManager = MyUserManager.getInstance()
        connectionManager = ConnectionManager.getInstance()


        hubConnectionViewModel = ViewModelProvider(this)[HubConnectionViewModel::class.java]
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]


    }

    //Login Button Click
    fun loginClick(view : View){
        val username = binding.userNameText.text.toString()
        if(binding.userNameText.text.isNotEmpty()){

            login(username)

        }else{
            Toast.makeText(this@LoginActivity , "Lütfen kullanıcı adınızı giriniz" , Toast.LENGTH_LONG).show()
        }
    }

    //Login işlemi için gerekli kontroller
    private fun login(username : String){

        //Login işlemi
        loginViewModel.loginPost(username)

        //Login işleminin dinlenme anı
        loginViewModel.loginStatus.observe(this){loginStatus->

            if (loginStatus == LoginStatus.SUCCESS){

                //Server'a (Socket) bağlanan metod
                hubConnectionViewModel.connectionServer(username)

                //Server'a bağlanmayı dinleyen metod
                hubConnectionViewModel.connectionStatus.observe(this){connectionStatus->

                    if(connectionStatus == LoginStatus.SUCCESS){

                        //Intent
                        val intent = Intent(this@LoginActivity , TalksActivity::class.java)
                        startActivity(intent)
                        finish()

                    }else{
                        Toast.makeText(this,"Sunucuya bağlanma işleminde hata meydana geldi",Toast.LENGTH_LONG).show()
                    }
                }
            }else{
                Toast.makeText(this@LoginActivity , "Login işleminde hata meydana geldi",Toast.LENGTH_LONG).show()
            }
        }
    }

    //SignUp ekranı açılması için metod
    fun goToSignUpActivity(view: View){

        val intent = Intent(this , SignUpActivity::class.java)
        startActivity(intent)
    }
}