package com.example.myapplication.domain

import android.graphics.Bitmap

interface LandmarkClassifier {

    fun classifier(bitmap: Bitmap, rotation: Int): List<Classification>
}