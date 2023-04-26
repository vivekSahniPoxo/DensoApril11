package com.example.denso.api

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(): Interceptor {


    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
       request.addHeader("Content-Type", "application/json")
//         request.addHeader("Content-Type", "text/json")
     //   request.addHeader("Content-Type", "text/plain")

        return chain.proceed(request.build())
    }
}