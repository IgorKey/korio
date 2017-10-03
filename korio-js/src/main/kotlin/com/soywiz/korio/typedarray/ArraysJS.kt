package com.soywiz.korio.typedarray

actual object Arrays {
	actual fun <T> copyRangeTo(src: Array<T>, srcPos: Int, dst: Array<T>, dstPos: Int, count: Int) {
		for (n in 0 until count) dst[dstPos + n] = src[srcPos + n]
	}

	actual fun copyRangeTo(src: BooleanArray, srcPos: Int, dst: BooleanArray, dstPos: Int, count: Int) {
		for (n in 0 until count) dst[dstPos + n] = src[srcPos + n]
	}

	actual fun copyRangeTo(src: ByteArray, srcPos: Int, dst: ByteArray, dstPos: Int, count: Int) {
		for (n in 0 until count) dst[dstPos + n] = src[srcPos + n]
	}

	actual fun copyRangeTo(src: ShortArray, srcPos: Int, dst: ShortArray, dstPos: Int, count: Int) {
		for (n in 0 until count) dst[dstPos + n] = src[srcPos + n]
	}

	actual fun copyRangeTo(src: IntArray, srcPos: Int, dst: IntArray, dstPos: Int, count: Int) {
		for (n in 0 until count) dst[dstPos + n] = src[srcPos + n]
	}

	actual fun copyRangeTo(src: LongArray, srcPos: Int, dst: LongArray, dstPos: Int, count: Int) {
		for (n in 0 until count) dst[dstPos + n] = src[srcPos + n]
	}

	actual fun copyRangeTo(src: FloatArray, srcPos: Int, dst: FloatArray, dstPos: Int, count: Int) {
		for (n in 0 until count) dst[dstPos + n] = src[srcPos + n]
	}

	actual fun copyRangeTo(src: DoubleArray, srcPos: Int, dst: DoubleArray, dstPos: Int, count: Int) {
		for (n in 0 until count) dst[dstPos + n] = src[srcPos + n]
	}

	actual fun <T> fill(src: Array<T>, value: T, from: Int, to: Int) {
		for (n in from until to) src[n] = value
	}

	actual fun fill(src: BooleanArray, value: Boolean, from: Int, to: Int) {
		for (n in from until to) src[n] = value
	}

	actual fun fill(src: ByteArray, value: Byte, from: Int, to: Int) {
		for (n in from until to) src[n] = value
	}

	actual fun fill(src: ShortArray, value: Short, from: Int, to: Int) {
		for (n in from until to) src[n] = value
	}

	actual fun fill(src: IntArray, value: Int, from: Int, to: Int) {
		for (n in from until to) src[n] = value
	}

	actual fun fill(src: FloatArray, value: Float, from: Int, to: Int) {
		for (n in from until to) src[n] = value
	}

	actual fun fill(src: DoubleArray, value: Double, from: Int, to: Int) {
		for (n in from until to) src[n] = value
	}
}