package com

import com.soywiz.korio.async.*
import com.soywiz.korio.lang.Thread_sleep
import com.soywiz.korio.net.ws.RawSocketWebSocketClient
import kotlinx.cinterop.StableRef
import kotlinx.coroutines.*
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.test.*

class WebSocketTest {
	@Test
	fun testWebSocket() = suspendTest {
		val start = Worker.start(true)
		start.execute(TransferMode.UNSAFE, {}) { input ->
			runBlocking {
				try {


				val ws = RawSocketWebSocketClient("ws://localhost:8081/myws/echo", null, null, null, false)
				ws.onStringMessage.add {
					println("onStringMessage $it")
				}
				ws.onError.add {
					it.printStackTrace()
				}

				ws.send("hello")
					println("send hello")
//				ws.send("hello1")
				kotlinx.coroutines.delay(5000)
				}catch (ex:Throwable){
					ex.printStackTrace()
				}
			}
		}.result



		Thread_sleep(5000)
		println("Workers: OK")


	}
}