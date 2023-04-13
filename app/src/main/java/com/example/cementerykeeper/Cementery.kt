package com.example.cementerykeeper


data class Cementery(
    var id: String = "",
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val user: String = "",
    var imageUrl: String = "",
    var score: Int = 0,
    var media: Int = 0
)