package example

import cn.ryoii.limiter.limit
import cn.ryoii.limiter.limitWith
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.event.subscribeMessages

suspend fun main() {
    val bot = BotFactory.newBot(0L, "")

    bot.eventChannel.subscribeMessages {

        // Common fallback
        val limiter = limitWith(limitPerMinute = 60, fallback = {
            subject.sendMessage("Limit... common")
        })

        // Limit ping1, ping2, ping3 called 1 time per second
        limit(case("ping1"), limiter) {
            subject.sendMessage("pong1")
        }

        limit(case("ping2"), limiter) {
            subject.sendMessage("pong2")
        }

        // Special fallback
        limit(case("ping3"), limiter, fallback = {
            subject.sendMessage("Limit... special for ping3")
        }) {
            subject.sendMessage("pong3")
        }
    }

    bot.run {
        login()
        join()
    }
}