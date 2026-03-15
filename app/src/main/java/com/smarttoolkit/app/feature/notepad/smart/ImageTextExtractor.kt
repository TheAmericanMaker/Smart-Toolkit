package com.smarttoolkit.app.feature.notepad.smart

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object ImageTextExtractor {

    suspend fun extractText(imageFile: File): String {
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            ?: throw IllegalArgumentException("Could not decode image")

        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        return suspendCancellableCoroutine { cont ->
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    cont.resume(result.text)
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }

            cont.invokeOnCancellation {
                recognizer.close()
            }
        }
    }

    fun splitIntoItems(text: String): List<String> {
        return text.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
}
