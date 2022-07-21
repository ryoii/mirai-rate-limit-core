package cn.ryoii.limiter

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class TestLimiter {

    @Test
    fun testTryAcquire() = runBlocking {
        val limiter = RateLimiter(0, 1, 1.seconds)
        val stop = AtomicBoolean(false)
        launch {
            delay(3100)
            stop.set(true)
        }

        var i = 0
        val res = mutableListOf<Int>()
        while (!stop.get()) {
            if (limiter.tryAcquire(1)) {
                res.add(i++)
            }
            delay(50)
        }

        assertEquals(listOf(0, 1, 2, 3), res)
    }

    @Test
    fun testAcquire() = runBlocking {
        val limiter = RateLimiter(0, 1, 1.seconds)
        val stop = AtomicBoolean(false)
        launch {
            delay(3100)
            stop.set(true)
        }

        var i = 0
        val res = mutableListOf<Int>()
        while (!stop.get()) {
            limiter.acquire(1)
            if (!stop.get()) {
                res.add(i++)
            }
        }

        assertEquals(listOf(0, 1, 2, 3), res)
    }

    @Test
    fun testWithCache() = runBlocking {
        val limiter = RateLimiter(1, 1, 1.seconds)
        var stop = false
        // filling cache
        delay(2.seconds)
        launch {
            delay(3.seconds)
            stop = true
        }

        var i = 0
        val res = mutableListOf<Int>()
        while (!stop) {
            limiter.acquire(1)
            if (!stop) {
                res.add(i++)
            }
        }

        assertEquals(listOf(0, 1, 2, 3, 4), res)
    }
}