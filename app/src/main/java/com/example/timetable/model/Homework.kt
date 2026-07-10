package com.example.timetable.model

import java.io.Serializable

data class Homework(
    var id: Int = 0,
    var subject: String = "",
    var title: String = "",
    var description: String = "",
    var date: String = "",
    var color: Int = 0,
    var completed: Int = 0
) : Serializable {
    constructor(subject: String, title: String, description: String, date: String, color: Int, completed: Int) : 
        this(0, subject, title, description, date, color, completed)
}
