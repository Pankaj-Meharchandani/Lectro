package com.example.timetable.model

import java.io.Serializable

data class Week(
    var id: Int = 0,
    var subject: String = "",
    var fragment: String = "",
    var teacher: String = "",
    var room: String = "",
    var fromTime: String = "",
    var toTime: String = "",
    var color: Int = 0,
    var time: String = ""
) : Serializable {
    constructor(subject: String?, teacher: String?, room: String?, fromTime: String?, toTime: String?, color: Int) : 
        this(0, subject ?: "", "", teacher ?: "", room ?: "", fromTime ?: "", toTime ?: "", color)

    override fun toString(): String {
        return subject
    }
}
