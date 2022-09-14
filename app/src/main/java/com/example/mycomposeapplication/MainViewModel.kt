package com.example.mycomposeapplication

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycomposeapplication.data.Example

class MainViewModel : ViewModel() {
    private val _strList = listOf("").toMutableStateList()
    val strList: List<String> = _strList

    fun execute(example: Example) {
        _strList.clear()
        example.execute(viewModelScope) {
            _strList.add(it)
        }
    }
}