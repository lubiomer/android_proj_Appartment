package com.management.roomates.data.model

data class User(
    var id: String = "",
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val phone: String = "",
    val apartmentId:String = ""
){
    override fun toString(): String {
        return name
    }
}
