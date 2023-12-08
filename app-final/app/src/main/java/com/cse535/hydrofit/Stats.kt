package com.cse535.hydrofit

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

enum class Gender {
    MALE, FEMALE;
}

data class Stats(
    val username: String = "",
    val name: String = "",
    val age: Int = 0,
    val weight: Int = 0,
    val height: Int = 0,
    val pace: Int = 0,
    val stepLength: Int = 0,
    val gender: Gender = Gender.MALE
)

fun Stats.toJson(): String {
    val gson = Gson()
    return gson.toJson(this)
}

fun String.toStats(): Stats {
    val gson = Gson()
    val type: Type = object : TypeToken<Stats>() {}.type
    return gson.fromJson(this, type)
}
