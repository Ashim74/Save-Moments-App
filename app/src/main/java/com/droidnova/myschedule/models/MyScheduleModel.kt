package com.droidnova.myschedule.models
import java.io.Serializable



data class HappyPlaceModel(
    val id: Int,
    val title: String,
    val image: String,
    val description: String,
    val date: String,
    
): Serializable
