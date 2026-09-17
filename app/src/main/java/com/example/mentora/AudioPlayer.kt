package com.example.mentora

import android.media.MediaPlayer
import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object AudioPlayer {
    suspend fun playBase64Audio(cacheDir: File, base64String: String?) = suspendCancellableCoroutine<Unit> { continuation ->
        if (base64String.isNullOrEmpty()) {
            Log.e("AudioPlayer", "The audio string is empty or null!")
            continuation.resume(Unit)
            return@suspendCancellableCoroutine
        }

        try {

            val cleanBase64 = base64String.substringAfter("base64,")
            val audioBytes = Base64.decode(cleanBase64, Base64.DEFAULT)

            val tempFile = File.createTempFile("mentora_audio", ".mp3", cacheDir)
            val fos = FileOutputStream(tempFile)
            fos.write(audioBytes)
            fos.close()

            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(tempFile.absolutePath)
            mediaPlayer.prepare()
            mediaPlayer.setOnCompletionListener {
                it.release()
                tempFile.delete()
                continuation.resume(Unit)
            }
            mediaPlayer.start()

        } catch (e: Exception) {

            Log.e("AudioPlayer", "Problem playing the audio: ", e)
            continuation.resume(Unit)
        }
    }
}