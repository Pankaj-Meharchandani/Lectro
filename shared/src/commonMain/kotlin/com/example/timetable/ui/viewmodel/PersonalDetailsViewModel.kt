package com.example.timetable.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.timetable.data.TimetableDatabase
import com.example.timetable.model.UserDetail
import com.example.timetable.model.UserFile

class PersonalDetailsViewModel(
    private val db: TimetableDatabase
) : ViewModel() {
    
    var userDetail by mutableStateOf(UserDetail())
    val userFiles = mutableStateListOf<UserFile>()

    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var roll by mutableStateOf("")
    var other by mutableStateOf("")
    var photoPath by mutableStateOf<String?>(null)

    fun loadData() {
        userDetail = db.getUserDetail()
        name = userDetail.name
        email = userDetail.email
        roll = userDetail.rollNumber
        other = userDetail.other
        photoPath = if (userDetail.photoPath.isNotEmpty()) userDetail.photoPath else null
        
        userFiles.clear()
        userFiles.addAll(db.getAllUserFiles())
    }

    fun updateField(
        newName: String = name,
        newEmail: String = email,
        newRoll: String = roll,
        newOther: String = other,
        newPhoto: String? = photoPath
    ) {
        name = newName
        email = newEmail
        roll = newRoll
        other = newOther
        photoPath = newPhoto
        saveDetails()
    }

    private fun saveDetails() {
        userDetail = userDetail.copy(
            name = name,
            email = email,
            rollNumber = roll,
            other = other,
            photoPath = photoPath ?: ""
        )
        db.saveUserDetail(userDetail)
    }

    fun addFile(title: String, path: String) {
        db.insertUserFile(UserFile(title = title, path = path))
        loadData()
    }

    fun deleteFile(id: Int) {
        db.deleteUserFile(id)
        loadData()
    }
}
