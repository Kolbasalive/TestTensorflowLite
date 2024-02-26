package com.example.myapplication.data

import android.content.Context
import android.graphics.Bitmap
import android.view.Surface
import com.example.myapplication.domain.Classification
import com.example.myapplication.domain.LandmarkClassifier
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

class TfLiteLandmarkClassifier(
    private val context: Context,
    private val threshold: Float = 0.5f,
    private val maxResults: Int = 1,

): LandmarkClassifier {

    private var classifier: ImageClassifier? = null

    private fun setupClassifier(){
        val baseOptions = BaseOptions.builder()
            .setNumThreads(2)
            .build()
        val options = ImageClassifier.ImageClassifierOptions.builder()
            .setBaseOptions(baseOptions)
            .setMaxResults(maxResults)
            .setScoreThreshold(threshold)
            .build()
        try {
            classifier = ImageClassifier.createFromFileAndOptions(
                context,
                "tensor.tflite",
                options
            )

        }catch (e: IllegalStateException){

        }
    }

    override fun classifier(bitmap: Bitmap, rotation: Int): List<Classification> {
        if (classifier == null){
            setupClassifier()
        }

        val imageProcess = ImageProcessor.Builder().build()
        val tensorImage = imageProcess.process(TensorImage.fromBitmap(bitmap))

        val imageProcessingOptions = ImageProcessingOptions.builder()
            .setOrientation(getOrientationsFromRotation(rotation))
            .build()

        val result = classifier?.classify(tensorImage, imageProcessingOptions)

        return result?.flatMap { classicatons ->
            classicatons.categories.map {category ->
                Classification(
                    name = category.displayName,
                    score = category.score
                )
            }
        }?.distinctBy {it.name} ?: emptyList()
    }

    private fun getOrientationsFromRotation(rotation: Int): ImageProcessingOptions.Orientation{
        return when(rotation){
            Surface.ROTATION_270 -> ImageProcessingOptions.Orientation.BOTTOM_RIGHT
            Surface.ROTATION_90 -> ImageProcessingOptions.Orientation.TOP_LEFT
            Surface.ROTATION_180 -> ImageProcessingOptions.Orientation.RIGHT_BOTTOM
            else -> ImageProcessingOptions.Orientation.RIGHT_TOP
        }
    }
}