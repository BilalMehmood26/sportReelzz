package com.usaclean.sportreelzz.model

data class User(
    var email :String = "",
    var id :String = "",
    var userName :String = "",
    var token :String = "",
    var lat: Double? = 0.0,
    var lng: Double? = 0.0,
    var image: String? = "",
    val memberSince :Long? = 0,
    var password :String = ""
    )
