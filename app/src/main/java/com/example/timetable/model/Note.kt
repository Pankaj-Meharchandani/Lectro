package com.example.timetable.model

import java.io.Serializable

data class Note(
    var id: Int = 0,
    var title: String = "",
    var text: String = "",
    var color: Int = 0,
    var subjectId: Int = -1
) : Serializable {
    constructor(title: String?, text: String, color: Int) : this(0, title ?: "", text, color, -1)
    constructor(title: String?, text: String, color: Int, subjectId: Int) : this(0, title ?: "", text, color, subjectId)
}
