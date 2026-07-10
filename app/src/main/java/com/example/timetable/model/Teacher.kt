package com.example.timetable.model
import java.io.Serializable
data class Teacher(
    var id: Int = 0,
    var name: String = "",
    var post: String = "",
    var phonenumber: String = "",
    var email: String = "",
    var cabinNumber: String = "",
    var color: Int = 0
) : Serializable {
    constructor(name: String, post: String, phonenumber: String, email: String, cabinNumber: String, color: Int) : 
        this(0, name, post, phonenumber, email, cabinNumber, color)
}
