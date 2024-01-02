package com.celalkorucu.xxx.ViewModel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.celalkorucu.xxx.Activitys.HTTP_REQUEST_BASE_URL
import com.celalkorucu.xxx.Helper.LoginStatus
import com.celalkorucu.xxx.Manager.MyUserManager
import com.celalkorucu.xxx.Model.Data
import com.celalkorucu.xxx.Model.ReadContactDto
import com.celalkorucu.xxx.Model.ResultItem
import com.celalkorucu.xxx.Model.User
import com.celalkorucu.xxx.Service.ServiceAPI
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginViewModel: ViewModel()  {

    //Manager
    private  var myUserManager: MyUserManager = MyUserManager.getInstance()

    //Login işlemini dinleyen metod
    private val _loginStatus = MutableLiveData<LoginStatus>()
    val loginStatus: LiveData<LoginStatus> get() = _loginStatus


    //Verilen kullanıcı adına göre server'a post işlemini gerçekleştirip myUser nesnesini oluşturan metod
     fun loginPost(username : String){

        val apiService = Retrofit.Builder()
            .baseUrl(HTTP_REQUEST_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServiceAPI::class.java)
        val readContactDto = ReadContactDto(username)
        val call = apiService.readContact(readContactDto)


        call.enqueue(object : Callback<ResultItem> {
            override fun onResponse(call: Call<ResultItem>, response: Response<ResultItem>) {

                val resultItem = response.body()

                resultItem.let {

                    val data : Data = it!!.data  as Data

                    val profilePhotoBitmap = string64ToBitmap(data.image)
                    val user = User(profilePhotoBitmap,username)

                    myUserManager.setCurrentUser(user)

                    _loginStatus.postValue(LoginStatus.SUCCESS)

                }
            }
            override fun onFailure(call: Call<ResultItem>, t: Throwable) {
                t.printStackTrace()
                _loginStatus.postValue(LoginStatus.ERROR)
            }
        })
    }

    //String bir biçimde verilen fotoğrafı Bitmap türüne çeviren metod
    fun string64ToBitmap(string64Image: String): Bitmap {
        val byteArray = Base64.decode(string64Image, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

}