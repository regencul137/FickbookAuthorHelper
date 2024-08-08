package com.example.fickbookauthorhelper.logic

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

class ImageLoader @Inject constructor(@ApplicationContext private val applicationContext: Context) {
    suspend fun downloadImage(
        url: String,
        directoryName: String,
        fileName: String
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()

                val inputStream: InputStream = connection.inputStream
                val directory = File(applicationContext.filesDir, directoryName)
                if (!directory.exists()) {
                    directory.mkdirs()
                }

                val file = File(directory, fileName)
                val outputStream = FileOutputStream(file)
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object ImageLoaderModule {
    @Singleton
    @Provides
    fun provideImageLoader(@ApplicationContext applicationContext: Context): ImageLoader {
        return ImageLoader(applicationContext)
    }
}