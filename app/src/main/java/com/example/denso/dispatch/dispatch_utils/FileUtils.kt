package com.example.denso.dispatch.dispatch_utils

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class FileUtils {
    /**
     * Check file name is valid
     * @param text file name check
     * @return true or false
     */
    private fun isValidName(text: String): Boolean {
        val pattern = Pattern.compile(
            "^(?!(?:CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\.[^.]*)?$)[^<>:\"/\\\\|?*\\x00-\\x1F]*[^<>:\"/\\\\|?*\\x00-\\x1F\\ .]$",
            Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE or Pattern.COMMENTS
        )
        val matcher = pattern.matcher(text)
        return matcher.matches()
    }

    /**
     * Save the displayed list UII to the specified in CSV format.
     * @param context Context
     * @param fileName file name to save.
     * @param listUII list data tags.
     * @return success or failed.
     */
    fun outputFile(context: Context?, fileName: String, listUII: ArrayList<String>): Boolean {
        var fileName = fileName
        val dir = File(FILE_PATH)
        // Check path is exits
        if (!dir.exists()) {
            // Create folder
            dir.mkdirs()
        }
        // Check file name has extension
        if (!isValidName(fileName)) {
            return false
        }
        if (!fileName.contains(FILE_EXTENSION)) {
            fileName += FILE_EXTENSION
        }
        val fileOutput = File(FILE_PATH + fileName)
        // Check file is exits
        if (!fileOutput.exists()) {
            try {
                fileOutput.createNewFile()
            } catch (e: IOException) {
                return false
            }
        }
        try {
            if (listUII.size > 0) {
                // Start write file result and implement
                val bufferedWriterLogResult = BufferedWriter(FileWriter(fileOutput, true))
                for (i in listUII.indices) {
                    bufferedWriterLogResult.append(String.format("%s", listUII[i] + ","))
                    bufferedWriterLogResult.newLine()
                }
                // Close file log
                bufferedWriterLogResult.close()
                MediaScannerConnection.scanFile(context, arrayOf(fileOutput.toString()), null, null)
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }

    /**
     * Read all data in file master.
     * @param fileMater file master selected.
     * @return dataFileMaster data in file Master.
     */
    fun readFileMaster(context: Context, fileMater: Uri?): HashSet<String> {
        val dataFileMaster = HashSet<String>()
        var line: String
        try {
            val input = InputStreamReader(
                context.contentResolver.openInputStream(
                    fileMater!!
                ), "Shift-JIS"
            )
            val bufferedReader = BufferedReader(input)
            while (bufferedReader.readLine().also { line = it } != null) {
                if ("" != line) {
                    dataFileMaster.add(line.replace(",", ""))
                }
            }
            bufferedReader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dataFileMaster
    }

    /**
     * Implement write file result output.
     * @param context Context
     * @param fileName File name result output.
     * @param listOrigin List data in file master.
     * @param listRead List data when read Tags.
     * @return success or fail.
     */
    fun writeFileOutputResult(
        context: Context?,
        fileName: String,
        listOrigin: HashSet<String>?,
        listRead: HashSet<String>?
    ): Boolean {

        // Get data uncounted for list data master and list data read.
        val listUncounted = splitArray(ArrayList(listOrigin), ArrayList(listRead))
        // Get data unlisted for list data master and list data read.
        val listUnlisted = splitArray(ArrayList(listRead), ArrayList(listOrigin))
        val dir = File(FILE_PATH)
        // Check path is exits
        if (!dir.exists()) {
            // Create folder
            dir.mkdirs()
        }
        val fileOutput = File(FILE_PATH + fileName)
        // Check file is exits
        if (!fileOutput.exists()) {
            try {
                fileOutput.createNewFile()
            } catch (e: IOException) {
                return false
            }
        }
        try {
            // Start write file result and implement
            val bufferedWriterLogResult = BufferedWriter(FileWriter(fileOutput, true))
            bufferedWriterLogResult.append(
                String.format(
                    "%s", SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(
                        Date()
                    )
                )
            )
            bufferedWriterLogResult.newLine()
            // Write data uncounted
            bufferedWriterLogResult.append(String.format("%s", "# uncounted"))
            bufferedWriterLogResult.newLine()
            for (i in listUncounted.indices) {
                bufferedWriterLogResult.append(String.format("%s", listUncounted[i]))
                bufferedWriterLogResult.newLine()
            }
            // Write data unlisted
            bufferedWriterLogResult.append(String.format("%s", "# unlisted"))
            bufferedWriterLogResult.newLine()
            for (i in listUnlisted.indices) {
                bufferedWriterLogResult.append(String.format("%s", listUnlisted[i]))
                bufferedWriterLogResult.newLine()
            }
            // Close file log
            bufferedWriterLogResult.close()
            MediaScannerConnection.scanFile(context, arrayOf(fileOutput.toString()), null, null)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    /**
     * Convert data from origin for list data uncounted and unlisted.
     * @param listFirst List data first to process.
     * @param listSecond List data second to process.
     * @return list result is uncounted or unlisted.
     */
    private fun splitArray(
        listFirst: ArrayList<String>,
        listSecond: ArrayList<String>
    ): ArrayList<String> {
        val listResult = ArrayList<String>()
        for (i in listFirst.indices) {
            var isCheck = false
            for (j in listSecond.indices) {
                // if item in list first but not in list second
                if (listFirst[i].equals(listSecond[j], ignoreCase = true)) {
                    isCheck = true
                }
            }
            if (!isCheck) {
                listResult.add(listFirst[i])
            }
        }
        return listResult
    }

    companion object {
        private val FILE_PATH = Environment.getExternalStorageDirectory().toString() + "/SP1Sample/"
        private const val FILE_EXTENSION = ".csv"
    }
}