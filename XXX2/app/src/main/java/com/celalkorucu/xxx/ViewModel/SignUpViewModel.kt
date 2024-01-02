package com.celalkorucu.xxx.ViewModel

import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.celalkorucu.xxx.Activitys.HTTP_REQUEST_BASE_URL
import com.celalkorucu.xxx.Model.ResultItem
import com.celalkorucu.xxx.Model.CreateContactDto
import com.celalkorucu.xxx.Service.ServiceAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class SignUpViewModel: ViewModel()  {


    //Kayıt olma işlemini dinleyen değişken
    private val _signUpControl = MutableLiveData<Boolean>()
    val signUpControl : LiveData<Boolean> get() = _signUpControl


    //ByteArray şeklinde verilen fotoğraf ve kullanıcı adına göre sunucuya post işlemi yapıp kayıt olmamızı sağlar
    fun signUp(byteArray: ByteArray, username : String){

        //İşlem bitesiye kadar bekler
        runBlocking {
            launch(Dispatchers.IO) {
                try {

                    val string64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
                    val userRegister = CreateContactDto(string64, username)

                    val apiService: ServiceAPI =
                        Retrofit.Builder()
                            .baseUrl(HTTP_REQUEST_BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                            .create(ServiceAPI::class.java)

                    val call = apiService.createContact(userRegister)
                    call.enqueue(object : Callback<ResultItem> {
                        override fun onResponse(
                            call: Call<ResultItem>,
                            response: Response<ResultItem>
                        ) {
                            val apiResponse = response.body()
                            apiResponse.let {

                                it!!.isSuccess?.let {control->
                                    if (control){
                                        _signUpControl.postValue(true)
                                    }
                                }
                            }
                        }
                        override fun onFailure(call: Call<ResultItem>, t: Throwable) {
                            println("Kayıt olma işlemi başarısız")
                            t.printStackTrace()
                            _signUpControl.postValue(false)
                        }
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("Kayıt olma hatası")
                }
            }
        }
    }
}