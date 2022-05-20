package cn.ryoii.limiter

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Duration.Companion.seconds

class TestLimiter {

    @Test
    fun testTryAcquire() = runBlocking {
        val limiter = RateLimiter(0, 1, 1.seconds)
        var stop = false
        launch {
            delay(3.seconds)
            stop = true
        }

        var i = 0
        val res = mutableListOf<Int>()
        while (!stop) {
            if (limiter.tryAcquire(1)) {
                res.add(i++)
            }
            delay(100)
        }

        assertEquals(listOf(0, 1, 2), res)
    }

    @Test
    fun testAcquire() = runBlocking {
        val limiter = RateLimiter(0, 1, 1.seconds)
        var stop = false
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

        assertEquals(listOf(0, 1, 2, 3), res)
    }
}