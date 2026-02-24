package com.example.matheasy.models

import android.widget.ImageView

data class MoneyData(
    val view: ImageView,
    val originalX: Float,
    val originalY: Float,
    val originalZ: Float,
    val value: Int,
    var insideDeposit: Boolean = false
)