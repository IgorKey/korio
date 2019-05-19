package com.soywiz.korio.net

import kotlinx.cinterop.*
import kotlinx.coroutines.*
import platform.posix.*
import platform.windows.LPADDRINFOVar
import platform.windows.addrinfo

class NativeSocket private constructor(internal val sockfd: SOCKET, private var endpoint: Endpoint) {
	companion object {
		init {
			init_sockets()
		}

		operator fun invoke(): NativeSocket {
			val socket = platform.windows.socket(
				platform.windows.AF_INET,
				platform.windows.SOCK_STREAM,
				platform.windows.IPPROTO_TCP
			)
			return NativeSocket(socket, Endpoint(IP(0, 0, 0, 0), 0))
		}

		suspend fun connect(host: String, port: Int) = NativeSocket().apply { connect(host, port) }
		suspend fun bound(host: String, port: Int) = NativeSocket().apply { bind(host, port) }
		//suspend fun listen(host: String, port: Int) = NativeSocket().listen(host, port)

		fun checkErrors(name: String = "") {
			val error = platform.windows.WSAGetLastError()
			if (error != 0) {
				error("WSA error($name): $error")
			}
		}
	}

	data class Endpoint(val ip: IP, val port: Int) {
		override fun toString(): String = "$ip:$port"
	}

	class IP(val data: UByteArray) {
		constructor(v0: Int, v1: Int, v2: Int, v3: Int) : this(
			ubyteArrayOf(
				v0.toUByte(),
				v1.toUByte(),
				v2.toUByte(),
				v3.toUByte()
			)
		)

		val v0 get() = data[0]
		val v1 get() = data[1]
		val v2 get() = data[2]
		val v3 get() = data[3]
		val str get() = "$v0.$v1.$v2.$v3"
		val value: Int get() = (v0.toInt() shl 0) or (v1.toInt() shl 8) or (v2.toInt() shl 16) or (v3.toInt() shl 24)
		//val value: Int get() = (v0.toInt() shl 24) or (v1.toInt() shl 16) or (v2.toInt() shl 8) or (v3.toInt() shl 0)
		override fun toString(): String = str

		companion object {
			fun fromHost(host: String): IP {
				memScoped {
					// gethostbyname unusable on windows
					val addr = allocArray<LPADDRINFOVar>(1)
					val res = platform.windows.getaddrinfo(host, null, null, addr)
					checkErrors("getaddrinfo")
					val info = addr[0]!!.pointed
					val inetaddr = info.ai_addr!!.pointed.sa_data
					return IP(
						ubyteArrayOf(
							inetaddr[0].toUByte(),
							inetaddr[1].toUByte(),
							inetaddr[2].toUByte(),
							inetaddr[3].toUByte()
						)
					)
				}
			}
		}
	}

	fun CPointer<sockaddr_in>.set(ip: IP, port: Int) {
		val addr = this
		addr.pointed.sin_family = platform.windows.AF_INET.convert()
		addr.pointed.sin_addr.S_un.S_addr = ip.value.toUInt()
		addr.pointed.sin_port = swapBytes(port.toUShort())
	}

	val connected get() = _connected

	fun connect(host: String, port: Int) {
		memScoped {
			val ip = IP.fromHost(host)
			val addr = allocArray<LPADDRINFOVar>(1)
			val alloc = alloc<platform.windows.addrinfo>()
			alloc.ai_family = AF_INET
			alloc.ai_socktype = SOCK_STREAM
			alloc.ai_protocol = IPPROTO_TCP
			println(host)
			println(port)
			val res = platform.windows.getaddrinfo(host, port.toString(), alloc.ptr, addr)
			checkErrors("getaddrinfo")
			val info = addr[0]!!.pointed
			val inetaddr = info.ai_addr!!
			val connected = platform.windows.connect(sockfd, inetaddr.uncheckedCast(), sockaddr_in.size.convert())
			checkErrors("connect")
			endpoint = Endpoint(ip, port)
			setSocketBlockingEnabled(false)
			if (connected != 0) {
				_connected = false
				error("Can't connect to $ip:$port ('$host')")
			}
			_connected = true
		}
	}

	fun bind(host: String, port: Int, backlog: Int = 10) {
		memScoped {
			val ip = IP.fromHost(host)
			val addr = alloc<sockaddr_in>()
			addr.ptr.set(ip, port)
			platform.posix.bind(sockfd, addr.ptr.reinterpret(), sockaddr_in.size.convert())
			checkErrors("bind")
			platform.posix.listen(sockfd, backlog)
			checkErrors("listen")
			setSocketBlockingEnabled(false)
		}
	}

	fun CPointer<sockaddr_in>.toEndpoint(): Endpoint {
		return Endpoint(
			IP(this.pointed.sin_addr.readValue().getBytes().toUByteArray()),
			swapBytes(this.pointed.sin_port.toUShort()).toInt()
		)
	}

	fun tryAccept(): NativeSocket? {
		memScoped {
			val addr = alloc<sockaddr>()
			val socklen = alloc<platform.windows.socklen_tVar>()
			socklen.value = sockaddr.size.convert()
			val fd = platform.posix.accept(sockfd, addr.ptr, socklen.ptr)
			checkErrors("accept")
			if (fd.toInt() < 0) {
				val errno = posix_errno()
				//println("accept: fd=$fd, errno=$errno")
				when (errno) {
					EWOULDBLOCK -> return null
					else -> error("Couldn't accept socket ($fd) errno=$errno")
				}
			}
			//println("accept: fd=$fd")
			return NativeSocket(fd, addr.ptr.reinterpret<sockaddr_in>().toEndpoint()).apply {
				setSocketBlockingEnabled(false)
			}
		}
	}

	val availableBytes
		get() = run {
			val bytes_available = intArrayOf(0, 0)
			platform.windows.ioctlsocket(sockfd, platform.windows.FIONREAD, bytes_available.refTo(0).uncheckedCast())
			checkErrors("ioctlsocket")
			bytes_available[0]
		}

	//val connected: Boolean
	//    get() {
	//        memScoped {
	//            if (!_connected) return false
	//            val errorPtr = allocArray<IntVar>(1)
	//            val lenPtr = longArrayOf(IntVar.size.convert())
	//            val retval = getsockopt(sockfd, SOL_SOCKET, SO_ERROR, errorPtr, lenPtr.refTo(0).uncheckedCast())
	//            return (retval == 0 || errorPtr[0] == 0)
	//        }
	//    }

	private var _connected = false

	fun recv(data: ByteArray, offset: Int = 0, count: Int = data.size - offset): Int {
		val result = platform.windows.recv(sockfd, data.refTo(offset), count.convert(), 0).toInt()
		checkErrors("recv")
		return result
	}

	fun recv(count: Int): ByteArray {
		val data = ByteArray(count)
		val len = recv(data)
		if (len < 0) {
			_connected = false
			error("Socket read error")
		}
		return data.copyOf(len.convert())
	}

	fun tryRecv(data: ByteArray, offset: Int = 0, count: Int = data.size - offset): Int {
		if (availableBytes <= 0) return -1
		return recv(data, offset, count)
	}

	fun tryRecv(count: Int): ByteArray? {
		if (availableBytes <= 0) return null
		return recv(count)
	}

	fun send(data: ByteArray, offset: Int = 0, count: Int = data.size - offset) {
		if (count <= 0) return

		memScoped {
			//val result = platform.windows.send(sockfd, data.refTo(offset), count.convert(), 0)
			val result = platform.posix.send(sockfd, data.refTo(offset), count.convert(), 0)
			checkErrors("send")
			if (result < count) {
				_connected = false
				error("Socket write error")
			}
		}
	}

	fun close() {
		platform.windows.closesocket(sockfd)
		checkErrors("closesocket")
		//platform.posix.shutdown(sockfd, SHUT_RDWR)
		_connected = false
	}

	private fun setSocketBlockingEnabled(blocking: Boolean): Boolean {
		if (sockfd.toInt() < 0) return false
		memScoped {
			val mode = alloc<u_longVar>()
			mode.value = if (blocking) 0.convert() else 1.convert()
			val result = (platform.windows.ioctlsocket(sockfd, platform.windows.FIONBIO.convert(), mode.ptr) == 0)
			checkErrors("ioctlsocket")
			return result
		}
	}

	fun getLocalEndpoint(): Endpoint {
		memScoped {
			val localAddress = alloc<sockaddr_in>()
			val addressLength = alloc<platform.windows.socklen_tVar>()
			addressLength.value = sockaddr_in.size.convert()
			val result = platform.windows.getsockname(sockfd, localAddress.ptr.reinterpret(), addressLength.ptr)
			checkErrors("getsockname")
			if (result < 0) {
				return Endpoint(IP(0, 0, 0, 0), 0)
			}
			val ip = localAddress.sin_addr.readValue()
			val port = swapBytes(localAddress.sin_port)
			//println("result: $result")
			//println("local address: " + inet_ntoa(localAddress.sin_addr.readValue())?.toKString())
			//println("local port: " + )
			return Endpoint(IP(ip.getBytes().toUByteArray()), port.toInt())
		}
	}

	fun getRemoveEndpoint() = endpoint

	private fun swapBytes(v: UShort): UShort =
		(((v.toInt() and 0xFF) shl 8) or ((v.toInt() ushr 8) and 0xFF)).toUShort()

	override fun toString(): String = "NativeSocket(local=${getLocalEndpoint()}, remote=${getRemoveEndpoint()})"
}

suspend fun NativeSocket.suspendRecvUpTo(data: ByteArray, offset: Int = 0, count: Int = data.size - offset): Int {
	if (count <= 0) return count

	while (true) {
		val read = tryRecv(data, offset, count)
		if (read <= 0) {
			delay(10L)
			continue
		}
		return read
	}
}

suspend fun NativeSocket.suspendRecvExact(data: ByteArray, offset: Int = 0, count: Int = data.size - offset): Int {
	var position = offset
	var remaining = count
	while (true) {
		if (remaining <= 0) return count
		val read = suspendRecvUpTo(data, position, remaining)
		remaining -= read
		position += read
	}
}

suspend fun NativeSocket.suspendRecvExact(count: Int): ByteArray {
	return ByteArray(count).apply { suspendRecvExact(this) }
}

suspend fun NativeSocket.suspendRecvUpTo(count: Int): ByteArray {
	val out = ByteArray(count)
	val result = suspendRecvUpTo(out)
	return out.copyOf(result)
}

suspend fun NativeSocket.suspendSend(data: ByteArray, offset: Int = 0, count: Int = data.size - offset) {
	send(data, offset, count)
}

suspend fun NativeSocket.accept(): NativeSocket {
	while (true) {
		val socket = tryAccept()
		//println("suspendAccept: $socket")
		if (socket != null) return socket
		delay(10L)
	}
}

object NativeAsyncSocketFactory : AsyncSocketFactory() {
	class NativeAsyncClient(val socket: NativeSocket) : AsyncClient {
		override suspend fun connect(host: String, port: Int) {
			socket.connect(host, port)
		}

		override val connected: Boolean get() = socket.connected

		override suspend fun read(buffer: ByteArray, offset: Int, len: Int): Int {
			return socket.suspendRecvUpTo(buffer, offset, len)
		}

		override suspend fun write(buffer: ByteArray, offset: Int, len: Int) {
			socket.suspendSend(buffer, offset, len)
		}

		override suspend fun close() {
			socket.close()
		}
	}

	class NativeAsyncServer(val socket: NativeSocket, override val requestPort: Int, override val backlog: Int) :
		AsyncServer {
		override val host: String get() = socket.getLocalEndpoint().ip.str
		override val port: Int get() = socket.getLocalEndpoint().port

		override suspend fun accept(): AsyncClient {
			return NativeAsyncClient(socket.accept())
		}
	}

	override suspend fun createClient(secure: Boolean): AsyncClient {
		return NativeAsyncClient(NativeSocket())
	}

	override suspend fun createServer(port: Int, host: String, backlog: Int, secure: Boolean): AsyncServer {
		val socket = NativeSocket()
		socket.bind(host, port, backlog)
		return NativeAsyncServer(socket, port, backlog)
	}
}
