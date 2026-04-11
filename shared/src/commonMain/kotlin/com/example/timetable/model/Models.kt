package com.example.timetable.model

import kotlinx.serialization.Serializable

@Serializable
data class Exam(
    var id: Int = 0,
    var subject: String = "",
    var teacher: String = "",
    var time: String = "",
    var date: String = "",
    var room: String = "",
    var color: Int = 0
)

@Serializable
data class Note(
    var id: Int = 0,
    var title: String = "",
    var text: String = "",
    var color: Int = 0,
    var subjectId: Int = -1
)

@Serializable
data class Week(
    var id: Int = 0,
    var subject: String = "",
    var fragment: String = "",
    var teacher: String = "",
    var room: String = "",
    var fromTime: String = "",
    var toTime: String = "",
    var time: String = "",
    var color: Int = 0
)

@Serializable
data class Teacher(
    var id: Int = 0,
    var name: String = "",
    var post: String = "",
    var phonenumber: String = "",
    var email: String = "",
    var cabinNumber: String = "",
    var color: Int = 0
)

@Serializable
data class Homework(
    var id: Int = 0,
    var subject: String = "",
    var title: String = "",
    var description: String = "",
    var date: String = "",
    var color: Int = 0,
    var completed: Int = 0 // 0 for false, 1 for true
)

@Serializable
data class Material(
    var id: Int = 0,
    var subjectId: Int = 0,
    var path: String = "",
    var type: String = "",
    var name: String = ""
)

@Serializable
data class UserFile(
    var id: Int = 0,
    var title: String = "",
    var path: String = ""
)

@Serializable
data class UserDetail(
    var id: Int = 0,
    var name: String = "",
    var email: String = "",
    var rollNumber: String = "",
    var photoPath: String = "",
    var other: String = ""
)
