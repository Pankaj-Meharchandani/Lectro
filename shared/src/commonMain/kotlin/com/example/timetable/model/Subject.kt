package com.example.timetable.model

import kotlinx.serialization.Serializable

@Serializable
data class Subject(
    var id: Int = 0,
    var name: String = "",
    var color: Int = 0,
    var teacher: String = "",
    var room: String = "",
    var attended: Int = 0,
    var missed: Int = 0,
    var skipped: Int = 0
)
