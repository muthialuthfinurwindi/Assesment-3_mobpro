package com.muthia0027.mobpro1.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

private const val CLOUDINARY_BASE_URL =
    "https://api.cloudinary.com/v1_1/dac3joe5t/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val cloudinaryRetrofit = Retrofit.Builder()
    .baseUrl(CLOUDINARY_BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

interface CloudinaryApiService {

    @Multipart
    @POST("image/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("upload_preset") uploadPreset: RequestBody
    ): CloudinaryResponse
}

object CloudinaryApi {
    val service: CloudinaryApiService by lazy {
        cloudinaryRetrofit.create(CloudinaryApiService::class.java)
    }
}