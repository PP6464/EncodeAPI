package com.herokuapp.plugins

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.Identity.decode
import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.api.linalg.*
import org.jetbrains.kotlinx.multik.ndarray.operations.*
import java.net.URLEncoder
import java.nio.charset.*
import java.util.*
import kotlin.math.*

fun Application.configureRouting() {
	val encode = mk.ndarray(mk[listOf(420.0, 15.0), listOf(78.0, 69.0)])
	val det = 27810.0
	val decode = mk.ndarray(mk[listOf(69 / det, -15 / det), listOf(-78 / det, 420 / det)])
	
	val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
	
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
			val token = (1..20)
				.map { kotlin.random.Random.nextInt(0, charPool.size) }
				.map(charPool::get)
				.joinToString("")
			call.respondText { String(Base64.getEncoder().encode(token.toByteArray())) + ":" + String(Base64.getEncoder().encode(list.map { it.roundToInt() }.joinToString(".").toByteArray())) }
		}
		get("/decode") {
			val encoded = call.request.queryParameters["msg"] as String
			val msg = String(Base64.getDecoder().decode(encoded.split(":")[1].toByteArray()))
			val token = try {
				String(Base64.getDecoder().decode(encoded.split(":")[0].toByteArray()))
			} catch (e: Exception) {
				call.respondText { "Please provide a valid token" }
				return@get
			}
			val providedToken = String(Base64.getDecoder().decode(call.request.queryParameters["token"]))
			if (token.length - providedToken.length != 0 || token.indices.map { token[it] == providedToken[it] }.contains(false)) {
				call.respondText { "Please provide a valid token" }
				return@get
			}
			assert(msg.length % 2 == 0) // Make sure message can be converted into nx2 matrix
			val msgASCII = msg.split(".").map { it.toDouble() }.toList()
			val msgMatrix = mk.ndarray(msgASCII.chunked(msgASCII.size / 2))
			val dot = mk.linalg.dot(decode, msgMatrix).transpose()
			val d2List = dot.toListD2()
			val list = mutableListOf<Double>()
			d2List.map(list::addAll)
			call.respondText { list.map { it.roundToInt().toChar() }.joinToString("").dropLastWhile { it.toString() == "\u200B" } }
		}
		get("/") {
			call.respondText { "This is an encoding api" }
		}
	}
}
