package com.example.denso.di

import android.content.Context
import android.net.ConnectivityManager
import com.example.denso.api.Apies
import com.example.denso.api.AuthInterceptor
import com.example.denso.utils.Cons
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {


    @Singleton
    @Provides
    fun provideConnectivityManager( @ApplicationContext context: Context): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(interceptor: AuthInterceptor): OkHttpClient {
        val httpBuilder = OkHttpClient.Builder().addInterceptor(interceptor)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).readTimeout(80,TimeUnit.SECONDS)
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
        return httpBuilder.protocols(mutableListOf(Protocol.HTTP_1_1)).build()
    }




    var gson = GsonBuilder()
        .setLenient()
        .create()

//    @Singleton
//    @Provides
//    fun provideList(): Apies {
//        return Retrofit.Builder().baseUrl(Cons.BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create(gson)).build()
//            .create(Apies::class.java)
//    }


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

}