package com.soywiz.korio.net

import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
internal actual val asyncSocketFactory: AsyncSocketFactory = NativeAsyncSocketFactory
