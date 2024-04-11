package com.example.denso.utils



import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class RetrofitClient {


    companion object {

        private var retrofitAuth: Retrofit? = null
        private var retrofitUser: Retrofit? = null
        private var retrofit: Retrofit? = null


        fun getResponseFromApi(): RetrofitApi {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(Cons.BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    //.addConverterFactory(GsonConverterFactory.create(gson))
                    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
                    .client(getClient(true))
                    .build()
            }
            return retrofit!!.create(RetrofitApi::class.java)
        }






        private fun getClient(addHeaders: Boolean): OkHttpClient {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor(logging)
                .connectTimeout(2, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
            if (addHeaders) {
                httpClient.addInterceptor { chain: Interceptor.Chain ->
                    val request = chain.request().newBuilder()
                    request.addHeader("Content-Type", "application/json")
                    //request.addHeader("Authorization", "Bearer " + (SharePref.get().getData(Cons.ACCESSTOKEN)))
                    chain.proceed(request.build())
                }


            }
            httpClient.addInterceptor { chain ->
                val requestTime = System.currentTimeMillis()
                val response = chain.proceed(chain.request())
                val responseTime = System.currentTimeMillis() - requestTime
                val responseTimeInSeconds = responseTime / 1000.0
                Log.d("APIResponseTime", "Response Time: $responseTime ms")
                Log.d("APIResponseTime", "Response Time: $responseTimeInSeconds Secound")
                response
            }
            return httpClient.build()

        }



    }

}




