package com.example.denso.di

import android.content.Context
import android.net.ConnectivityManager
import android.os.Environment
import android.util.Log
import com.example.denso.api.Apies
import com.example.denso.api.AuthInterceptor
import com.example.denso.utils.Cons
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class NetworkModule() {


    @Singleton
    @Provides
    fun provideConnectivityManager( @ApplicationContext context: Context): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }


    @Singleton
    @Provides
    fun provideOkHttpClient(interceptor: AuthInterceptor, @ApplicationContext context: Context): OkHttpClient {
        // Set up cache directory
        val cacheDirectory = File(context.cacheDir, "http-cache")
        val cacheSize = 10 * 1024 * 1024 // 10 MB

        val cache = Cache(cacheDirectory, cacheSize.toLong())

        val httpBuilder = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .readTimeout(80, TimeUnit.SECONDS)
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .cache(cache) // Set up cache

        httpBuilder.addInterceptor { chain ->
            val request = chain.request()

            // Log the request body
            val requestBody = request.body
            if (requestBody != null) {
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                val requestBodyString = buffer.readUtf8()
                Log.d("APIRequest", "Request Body: $requestBodyString")

            }

            val requestTime = System.currentTimeMillis()
            val response = chain.proceed(request)
            val responseTime = System.currentTimeMillis() - requestTime
            val responseTimeInSeconds = responseTime / 1000.0
            Log.d("APIResponseTime", "Response Time: $responseTime ms")
            Log.d("APIResponseTime", "Response Time: $responseTimeInSeconds Secound")
            response
        }

        return httpBuilder.protocols(mutableListOf(Protocol.HTTP_1_1)).build()
    }




//    @Singleton
//    @Provides
//    fun provideOkHttpClient(interceptor: AuthInterceptor, @ApplicationContext context: Context): OkHttpClient {
//        // Set up cache directory
//        val cacheDirectory = File(context.cacheDir, "http-cache")
//        val cacheSize = 10 * 1024 * 1024 // 10 MB
//
//        val cache = Cache(cacheDirectory, cacheSize.toLong())
//
//        val httpBuilder = OkHttpClient.Builder()
//            .addInterceptor(interceptor)
//            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
//            .readTimeout(80, TimeUnit.SECONDS)
//            .connectTimeout(2, TimeUnit.MINUTES)
//            .readTimeout(2, TimeUnit.MINUTES)
//            .writeTimeout(2, TimeUnit.MINUTES)
//            .cache(cache) // Set up cache
//
//
//        httpBuilder.addInterceptor { chain ->
//            val request = chain.request()
//
//            // Log the request body
//            val requestBody = request.body
//            if (requestBody != null) {
//                val buffer = Buffer()
//                requestBody.writeTo(buffer)
//                val requestBodyString = buffer.readUtf8()
//                Log.d("APIRequest", "Request Body: $requestBodyString")
//            }
//
//        httpBuilder.addInterceptor { chain ->
//            val requestTime = System.currentTimeMillis()
//            val response = chain.proceed(chain.request())
//            val responseTime = System.currentTimeMillis() - requestTime
//            val responseTimeInSeconds = responseTime / 1000.0
//            Log.d("APIResponseTime", "Response Time: $responseTime ms")
//            Log.d("APIResponseTime", "Response Time: $responseTimeInSeconds Secound")
//            response
//        }
//
//        return httpBuilder.protocols(mutableListOf(Protocol.HTTP_1_1)).build()
//    }




//    @Singleton
//    @Provides
//    fun provideOkHttpClient(interceptor: AuthInterceptor): OkHttpClient {
//        val httpBuilder = OkHttpClient.Builder().addInterceptor(interceptor)
//            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).readTimeout(80,TimeUnit.SECONDS)
//            .connectTimeout(2, TimeUnit.MINUTES)
//            .readTimeout(2, TimeUnit.MINUTES)
//            .writeTimeout(2, TimeUnit.MINUTES)
//
//
//
//        return httpBuilder.protocols(mutableListOf(Protocol.HTTP_1_1)).build()
//    }




    var gson = GsonBuilder()
        .setLenient()
        .create()




    @Singleton
    @Provides
    fun providesRetrofit(): Retrofit.Builder{
        return Retrofit.Builder().baseUrl(Cons.BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson)) }


    @Singleton
    @Provides
    fun providesAllApi(retrofitBuilder: Retrofit.Builder,okHttpClient: OkHttpClient):Apies{
        return retrofitBuilder.client(okHttpClient).build().create(Apies::class.java)
    }


    fun clearCache(context: Context) {
        val cacheDirectory = File(context.cacheDir, "http-cache")
        val cache = Cache(cacheDirectory, 10 * 1024 * 1024) // Same cache size as before
        cache.evictAll()
    }


    fun writeToFileExternal(context: Context, fileName: String, data: String) {
        try {
            val state = Environment.getExternalStorageState()
            if (Environment.MEDIA_MOUNTED != state) {
                Log.d("writeToFileExternal", "External storage is not available")
                return
            }

            val externalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (externalDir == null) {
                Log.d("writeToFileExternal", "External directory is null")
                return
            }

            val file = File(externalDir, fileName)

            val fileOutputStream = FileOutputStream(file, true)
            val outputStreamWriter = OutputStreamWriter(fileOutputStream)

            // Append a newline character before adding new content
            if (file.length() > 0) {
                outputStreamWriter.append('\n')
            }

            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



}