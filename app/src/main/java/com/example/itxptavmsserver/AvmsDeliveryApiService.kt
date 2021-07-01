package com.example.itxptavmsserver

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body

import retrofit2.http.POST
import retrofit2.http.Url

interface AvmsDeliveryApiService {
    @POST
    fun sendDelivery(@Url url: String, @Body requestBody: RequestBody): Call<ResponseBody>
}