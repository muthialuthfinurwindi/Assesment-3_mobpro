package com.muthia0027.mobpro1.network

import com.muthia0027.mobpro1.model.Item
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

private const val BASE_URL =
    "https://6a33ed758248ee962fa4b007.mockapi.io/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

interface ItemApiService {

    @GET("datas")
    suspend fun getItems(): List<Item>

    @POST("datas")
    suspend fun createItem(
        @Body item: ItemRequest
    ): Item

    @PUT("datas/{id}")
    suspend fun updateItem(
        @Path("id") id: String,
        @Body item: ItemRequest
    ): Item

    @DELETE("datas/{id}")
    suspend fun deleteItem(
        @Path("id") id: String
    ): Item
}

object ItemApi {
    val service: ItemApiService by lazy {
        retrofit.create(ItemApiService::class.java)
    }
}

enum class ApiStatus {
    LOADING,
    SUCCESS,
    ERROR
}