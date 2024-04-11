package com.example.denso.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import java.io.File
import java.io.FileWriter
import java.io.IOException

class RequestBodyInterceptor(private val context: Context) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val requestBody = request.body

        // Log the request body if it exists
        requestBody?.let {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            val requestBodyString = buffer.readUtf8()
            // Log the requestBodyString or perform any other action you need
            // For example, you can use Timber to log it or print it to the console
            val dataToLog = "Your data to be logged."
            val filename = "vivek.txt"

            logToFile(context, requestBodyString, filename)
        }

        return chain.proceed(request)
    }


    fun logToFile(context: Context, data: String, filename: String) {
        try {
            // Check if external storage is available for writing
            if (isExternalStorageWritable()) {
                // Get the external storage directory for your app
                val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

                // Create a file object for the specified filename
                val file = File(storageDir, filename)

                // Create a FileWriter to write to the file
                val fileWriter = FileWriter(file, true) // Set 'true' to append data to the file

                // Write the data to the file
                fileWriter.write(data)
                fileWriter.write("\n") // Add a new line after each entry (optional)

                // Close the file writer
                fileWriter.close()

                // Log success message
                println("Data logged to $filename successfully.")
            } else {
                println("External storage is not writable.")
            }
        } catch (e: IOException) {
            // Log any errors that occurred
            e.printStackTrace()
        }
    }

    fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }
}


//class RequestBodyInterceptor : Interceptor {
//    override fun intercept(chain: Interceptor.Chain): Response {
//        val originalRequest = chain.request()
//
//
//
//        // Modify the request body if needed
//        val modifiedRequestBody = modifyRequestBody(originalRequest.body)
//
//        // Create a new request with the modified request body
//        val newRequest = originalRequest.newBuilder()
//            .method(originalRequest.method, modifiedRequestBody)
//            .build()
//
//        return chain.proceed(newRequest)
//    }
//
//    private fun modifyRequestBody(requestBody: RequestBody?): RequestBody? {
//        // Implement the logic to modify the request body here if needed
//        // For example, you can add headers or encrypt data in this method
//
//        return requestBody
//    }
//}