package com.example.itxptavmsserver

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val DEFAULT_URL = "http://localhost/"
private const val DEFAULT_TIMEOUT_SEC: Long = 40
private const val XML_MEDIA_TYPE = "text/xml"

private const val TAG = "AVMS-DeliveryClient"

class AvmsDeliveryNetworkClient {

    private lateinit var service: AvmsDeliveryApiService

    init {
        initService()
    }

    private fun initService() {
        val builder = OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT_SEC, TimeUnit.SECONDS)
        addInterceptors(builder)
        val client = builder.build()

        service = Retrofit.Builder()
                .baseUrl(DEFAULT_URL)
                .client(client)
                .build()
                .create(AvmsDeliveryApiService::class.java)
    }

    private fun addInterceptors(builder: OkHttpClient.Builder) {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        builder.addInterceptor(loggingInterceptor)
    }

    fun sendDeliveryAndWaitForResult(url: String, delivery: IAvmsDelivery): Boolean {
        val requestBody: RequestBody = delivery.toXml().toRequestBody(XML_MEDIA_TYPE.toMediaTypeOrNull())
        return try {
            service.sendDelivery(url, requestBody).execute().isSuccessful
        } catch (e: IOException) {
            Log.d(TAG, "Delivery failed to $url")
            false
        }
    }
}