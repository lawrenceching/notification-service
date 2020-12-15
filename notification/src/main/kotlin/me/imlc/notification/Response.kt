package me.imlc.notification

import com.google.gson.GsonBuilder

private val gson = GsonBuilder().setPrettyPrinting().create()

data class Response(val message: String) {
    fun toJson(): String {
        return gson.toJson(this)
    }
}