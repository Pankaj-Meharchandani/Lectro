package com.example.timetable.model

import java.io.Serializable

data class Subject(
    var id: Int = 0,
    var name: String = "",
    var color: Int = 0,
    var teacher: String? = null,
    var room: String? = null,
    var attended: Int = 0,
    var missed: Int = 0,
    var skipped: Int = 0,
    var credits: Int = 0,
    var gradePoint: Double = 0.0
) : Serializable {
    // Secondary constructors for legacy Java calls
    constructor(name: String, color: Int, teacher: String?, room: String?) : this(0, name, color, teacher, room)
    
    constructor(name: String, color: Int, teacher: String?, room: String?, attended: Int, missed: Int, skipped: Int) : 
        this(0, name, color, teacher, room, attended, missed, skipped)

    constructor(name: String, color: Int, teacher: String?, room: String?, attended: Int, missed: Int, skipped: Int, credits: Int, gradePoint: Double) : 
        this(0, name, color, teacher, room, attended, missed, skipped, credits, gradePoint)
}
