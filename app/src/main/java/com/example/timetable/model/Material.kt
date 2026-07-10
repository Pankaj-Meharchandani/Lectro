package com.example.timetable.model

import java.io.Serializable

data class Material(
    var id: Int = 0,
    var subjectId: Int = 0,
    var path: String = "",
    var type: String = "",
    var name: String = ""
) : Serializable
