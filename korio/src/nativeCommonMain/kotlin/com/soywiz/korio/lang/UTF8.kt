package com.soywiz.korio.lang

import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
actual val UTF8: Charset =  UTC8CharsetBase("UTF-8")
