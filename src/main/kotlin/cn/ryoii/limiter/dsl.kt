package cn.ryoii.limiter

import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.MessageDsl
import net.mamoe.mirai.event.MessageSubscribersBuilder
import net.mamoe.mirai.event.events.MessageEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * 实例化一个限流器
 *
 * 同一个限流器可以重复使用，使用同一个限流器的请求会被归类为同一组，共享限制的资源
 *
 * @param M 消息类型
 * @param maxCache 限流器的最大缓存数量
 * @param limitPerMinute 每分钟限流请求数量
 * @param fallback 限流器的默认降级处理
 */
@Suppress("unused")
fun <M : MessageEvent> MessageSubscribersBuilder<M, Listener<M>, Unit, Unit>.limitWith(
    maxCache: Int = 1,
    limitPerMinute: Int,
    fallback: (suspend M.(String) -> Unit)? = null,
): FallBackRateLimit<M> = FallBackRateLimit(maxCache, limitPerMinute, 1.minutes, fallback)

/**
 * 实例化一个限流器
 *
 * 同一个限流器可以重复使用，使用同一个限流器的请求会被归类为同一组，共享限制的资源
 *
 * @param M 消息类型
 * @param maxCache 限流器的最大缓存数量
 * @param limitPerDuration 每个单位时间限流请求数量
 * @param duration 单位时间
 * @param fallback 限流器的默认降级处理
 */
@Suppress("unused")
fun <M : MessageEvent> MessageSubscribersBuilder<M, Listener<M>, Unit, Unit>.limitWithDuration(
    maxCache: Int = 1,
    limitPerDuration: Int,
    duration: Duration,
    fallback: (suspend M.(String) -> Unit)? = null,
): FallBackRateLimit<M> = FallBackRateLimit(maxCache, limitPerDuration, duration, fallback)

/**
 * 可限流监听器
 *
 * ```kotlin
 * bot.eventChannel.subscribeMessages {
 *     val limiter = limitWith(maxCache = 1, limitPerMinute = 60)
 *
 *     limit(case("ping"), limiter, fallback = {
 *         subject.sendMessage("Limited...")
 *     }) {
 *         subject.sendMessage("pong")
 *     }
 * }
 * ```
 *
 * @param filter 消息过滤器. 支持所有原本就可以用的过滤器
 * @param limiter 限流器. 可为多个监听器配置同一个限流器实例，以进行分组管理
 * @param weight 请求权重. 用于分组中，表示该请求相对于同一个分组中的其他请求占用更多的资源
 * @param fallback 方法降级. 该请求的降级方法，优先级高于 [FallBackRateLimit.fallback]
 * @param onEvent 事件处理逻辑
 */
@MessageDsl
fun <M : MessageEvent> MessageSubscribersBuilder<M, Listener<M>, Unit, Unit>.limit(
    filter: MessageSubscribersBuilder<M, Listener<M>, Unit, Unit>.ListeningFilter,
    limiter: FallBackRateLimit<M>,
    weight: Int = 1,
    fallback: (suspend M.(String) -> Unit)? = null,
    onEvent: suspend M.(String) -> Unit
): Listener<M> {
    return always {
        if (!filter.filter.invoke(this, it)) {
            return@always
        }

        if (limiter.fallback == null && fallback == null) {
            limiter.acquire(weight)
            onEvent.invoke(this, it)
        } else {
            if (limiter.tryAcquire(weight)) {
                onEvent.invoke(this, it)
            } else {
                limiter.fallback?.invoke(this, it)
                    ?: fallback?.invoke(this, it)
            }
        }
    }
}
