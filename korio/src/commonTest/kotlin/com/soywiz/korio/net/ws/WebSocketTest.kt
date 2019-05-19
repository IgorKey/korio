package com.soywiz.korio.net.ws

import com.soywiz.korio.async.*
import com.soywiz.korio.lang.toByteArray
import com.soywiz.korio.net.URL
import kotlinx.coroutines.*
import kotlin.test.*

class WebSocketTest {
	@Test
	@Ignore
	fun testWebSocket() = suspendTest {

//		val url = URL("http://localhost:8080/path1/path2")
//		println(url)

		//		val launch = GlobalScope.launch {
//			//		coroutineScope {
////			CoroutineScope()
//
//		}
//		launch.join()


//		coroutineScope {
////			println("xxx")
//		val ws = WebSocketClient("ws://host.docker.internal:8081/myws/echo")
//
//		ws.onStringMessage.add {
//				println(it.length)
//			}
////
////
//////			println("hi")
//		val s = "s"
//		val sb = StringBuilder()
//		for(i in 0 until 143072){
//			sb.append(s)
//		}
//		val message = sb.toString()
//		println("bytes to senddd:" + message.toByteArray().size)
//		ws.send(message)
////			ws.send("hello1")
////			ws.send("hello3")
////			ws.send("hello4")
////			ws.send("hello5")
////			ws.send("hello2")
//////			println(ws.readString())
//////			println("hi1")l
//////			ws.send("world")
//////			println("hi1")
//////			println(ws.readString())
//////			println(ws.readString())
//////			assertEquals("hello", ws.readString())
//////			println("hi3")
//////			assertEquals("world", ws.readString())
////			delay(6000)
////			ws.close()
////////		}
////
////
////		}
	}
}