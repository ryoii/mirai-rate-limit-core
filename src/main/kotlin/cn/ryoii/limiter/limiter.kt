package cn.ryoii.limiter

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.minutes

/**
 * 令牌桶限流器实现
 *
 * 根据 `limitPerMinute` 计算出每个令牌生成的时间间隔，通过间隔计算出每个请求进入时的可用令牌数。
 *
 * @param maxCache 最大令牌缓存数
 * @param limitPerMinute 平均每分钟限流数
 */
open class RateLimiter(maxCache: Int, limitPerMinute: Int) {
    private var stored: Int
    private val maxStored: Int
    private val interval: Long
    private var nextTime: Long
    private val mutex = Mutex()

    init {
        stored = 0
        maxStored = maxCache
        interval = 1.minutes.div(limitPerMinute).inWholeMilliseconds
        nextTime = now()
    }

    /***
     * 尝试获取令牌，不会导致请求挂起
     *
     * @param count 一次获取的令牌数，获取多个令牌说明该请求占用更多的资源，在同一个分组中占用更大的比重
     */
    suspend fun tryAcquire(count: Int): Boolean {
        check(count > 0) { "acquire count must be positive." }
        return calWaitTime(count) == 0L
    }

    /***
     * 尝试获取令牌，当没有令牌可用时，会挂起直到获取令牌成功
     *
     * @param count 一次获取的令牌数，获取多个令牌说明该请求占用更多的资源，在同一个分组中占用更大的比重
     */
    suspend fun acquire(count: Int): Long {
        check(count > 0) { "acquire count must be positive." }
        val waitMillis = calWaitTime(count)
        if (waitMillis > 0) {
            delay(waitMillis)
        }

        return waitMillis
    }

    private suspend fun calWaitTime(count: Int): Long {
        this.mutex.lock()
        val nowMillis = now()
        val momentAvailable = nextAvailableMoment(count, nowMillis)
        this.mutex.unlock()
        return max(momentAvailable - nowMillis, 0)
    }

    private fun nextAvailableMoment(count: Int, nowMillis: Long): Long {
        reSync(nowMillis)
        val returnVal = nextTime

        val consumed = min(count, this.stored)
        val subsist = count - consumed

        val waitTime = subsist * interval
        this.nextTime += waitTime

        this.stored -= consumed
        return returnVal
    }

    private fun reSync(nowMillis: Long) {
        if (nowMillis > nextTime) {
            stored = min(maxStored, stored + ((nowMillis - nextTime) / interval).toInt())
            nextTime = nowMillis
        }
    }

    private fun now() = System.currentTimeMillis()
}

/**
 * 带 fallback 的限流器
 *
 * @see [RateLimiter]
 */
class FallBackRateLimit<M>(
    maxCache: Int,
    limitPerMinute: Int,
    val fallback: (suspend M.(String) -> Unit)?
) : RateLimiter(maxCache, limitPerMinute)
