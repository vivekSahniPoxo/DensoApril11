package com.example.denso.api

import retrofit2.http.Query

interface UserApi {

    suspend fun getLogin(@Query("username")userName:String,@Query("password")password:String,
                         @Query("plantid")plantid:String)

}