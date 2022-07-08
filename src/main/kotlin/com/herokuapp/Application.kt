package com.herokuapp

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.herokuapp.plugins.*

fun main() {
	embeddedServer(Netty, port = System.getenv("PORT").toInt(), host = "0.0.0.0") {
		configureRouting()
	}.start(wait = true)
}
