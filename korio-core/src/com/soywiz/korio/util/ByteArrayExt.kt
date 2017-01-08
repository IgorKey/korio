package com.soywiz.korio.util

fun List<ByteArray>.join(): ByteArray {
	val out = ByteArray(this.sumBy { it.size })
	var pos = 0
	for (c in this) {
		System.arraycopy(c, 0, out, pos, c.size)
		pos += c.size
	}
	return out
}

val HEX_DIGITS = "0123456789ABCDEF"
fun ByteArray.toHexString(): String {
	val out = CharArray(this.size * 2)
	var m = 0
	for (n in this.indices) {
		val v = this[n].toInt() and 0xFF
		out[m++] = HEX_DIGITS[(v ushr 4) and 0xF]
		out[m++] = HEX_DIGITS[(v ushr 0) and 0xF]
	}
	return String(out)
}
