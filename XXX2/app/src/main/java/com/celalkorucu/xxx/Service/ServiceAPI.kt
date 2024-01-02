package com.celalkorucu.xxx.Service

import com.celalkorucu.xxx.Model.ResultItem
import com.celalkorucu.xxx.Model.CreateContactDto
import com.celalkorucu.xxx.Model.ReadContactDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ServiceAPI {



    @POST("CreateContact")
    fun createContact(@Body userToRegister  : CreateContactDto) : Call<ResultItem>


    @POST("UpdateContact")
    fun updateContact(@Body updateUserContact : CreateContactDto) : Call<ResultItem>



    @POST("ReadContact")
    fun readContact(@Body usernameRequest: ReadContactDto): Call<ResultItem>
}
