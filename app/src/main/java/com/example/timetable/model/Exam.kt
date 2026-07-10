package com.example.timetable.model
import java.io.Serializable
data class Exam(
    var id: Int = 0,
    var subject: String = "",
    var teacher: String = "",
    var time: String = "",
    var date: String = "",
    var room: String = "",
    var color: Int = 0
) : Serializable {
    constructor(subject: String, teacher: String, time: String, date: String, room: String, color: Int) : 
        this(0, subject, teacher, time, date, room, color)
}
