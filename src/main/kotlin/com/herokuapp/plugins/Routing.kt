package com.herokuapp.plugins

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.api.linalg.*
import org.jetbrains.kotlinx.multik.ndarray.operations.*
import java.util.*
import kotlin.math.*

fun Application.configureRouting() {
	val encode = mk.ndarray(mk[listOf(420.0, 15.0), listOf(78.0, 69.0)])
	val det = 27810.0
	val decode = mk.ndarray(mk[listOf(69 / det, -15 / det), listOf(-78 / det, 420 / det)])
	
	// Starting point for a Ktor app:
	routing {
		get("/encode") {
			var msg = call.request.queryParameters["msg"] as String
			if (msg.length % 2 != 0) msg += "\u200B"
			assert(msg.length % 2 == 0) // Make sure message can be converted into nx2 matrix
			val msgASCII = msg.map { it.code.toDouble() }.toList()
			val msgMatrix = mk.ndarray(msgASCII.chunked(2)).transpose()
			val dot = mk.linalg.dot(encode, msgMatrix)
			val d2List = dot.toListD2()
			val list = mutableListOf<Double>()
			d2List.map(list::addAll)
			call.respondText { String(Base64.getEncoder().encode(list.map { it.roundToInt() }.joinToString(".").toByteArray())) }
		}
		get("/decode") {
			val encoded = call.request.queryParameters["msg"] as String
			val msg = String(Base64.getDecoder().decode(encoded.toByteArray()))
			assert(msg.length % 2 == 0) // Make sure message can be converted into nx2 matrix
			val msgASCII = msg.split(".").map { it.toDouble() }.toList()
			val msgMatrix = mk.ndarray(msgASCII.chunked(msgASCII.size / 2))
			val dot = mk.linalg.dot(decode, msgMatrix).transpose()
			val d2List = dot.toListD2()
			val list = mutableListOf<Double>()
			d2List.map(list::addAll)
			call.respondText { list.map { it.roundToInt().toChar() }.joinToString("").dropLastWhile { it.toString() == "\u200B" } }
		}
	}
}
