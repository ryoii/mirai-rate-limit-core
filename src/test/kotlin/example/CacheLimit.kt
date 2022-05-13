package example

import cn.ryoii.limiter.limit
import cn.ryoii.limiter.limitWith
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.event.subscribeMessages

suspend fun main() {
    val bot = BotFactory.newBot(0L, "")

    bot.eventChannel.subscribeMessages {

        // Limit ping called 1 time per second with cache
        limit(case("ping"), limitWith(maxCache = 10, limitPerMinute = 60)) {
            subject.sendMessage("pong")
        }
    }

    bot.run {
        login()
        join()
    }
}