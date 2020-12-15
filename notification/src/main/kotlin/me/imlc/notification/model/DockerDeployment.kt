package me.imlc.notification.model

import com.google.gson.GsonBuilder

private val gson = GsonBuilder().setPrettyPrinting().create()

data class DockerDeployment(
        val name: String,
        val image: String
) {
    fun toJson(): String {
        return gson.toJson(this)
    }
}