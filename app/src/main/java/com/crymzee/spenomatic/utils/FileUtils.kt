package com.crymzee.drivetalk.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.DocumentsContract
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.io.readBytes
import kotlin.io.use
import kotlin.jvm.Throws

object FileUtils {
    @Throws
    fun createUri(context: Context, directory: Uri, mimeType: String, name: String): Uri {
        val docId = DocumentsContract.getTreeDocumentId(directory)
        val dirUri = DocumentsContract.buildDocumentUriUsingTree(directory, docId)
        return DocumentsContract.createDocument(
            context.contentResolver, dirUri, mimeType, name
        ) ?: throw FileNotFoundException()
    }

    fun writeToFile(
        context: Context, directory: Uri, mimeType: String, name: String, inputStream: InputStream
    ): Uri? {
        return try {
            val uri = createUri(context, directory, mimeType, name)
            context.contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { outputStream ->
                    outputStream.write(inputStream.readBytes())
                }
            }
            uri
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }


    fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun Fragment.saveImageToFile(imageUri: Uri): File? {
        val context = requireContext() // Get the Fragment's context
        val cacheDir = context.cacheDir
        val file = File(cacheDir, "compressed_${System.currentTimeMillis()}.jpg")

        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            var quality = 100
            val outputStream = FileOutputStream(file)

            do {
                outputStream.use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos)
                }
                val fileSize = file.length() / 1024 // Convert to KB

                if (fileSize > 2048) { // If file size > 2MB
                    quality -= 10 // Reduce quality by 10%
                } else {
                    break
                }
            } while (quality > 10) // Avoid very low quality

            return file // Return the compressed file

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null // Return null if saving fails
    }


}