package example

import cn.ryoii.limiter.limit
import cn.ryoii.limiter.limitWith
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.subscribeMessages

suspend fun main() {
    val bot = BotFactory.newBot(0L, "")

    bot.eventChannel.subscribeMessages {

        // Limit ping called 1 time per second
        limit(
            case("ping1"),
            limitWith(limitPerMinute = 60),
            fallback = {
                subject.sendMessage("Limited... Try again later.")
            }
        ) {
            subject.sendMessage("pong1")
        }

        // Use defined fallback
        val fallback: suspend MessageEvent.(String) -> Unit = {
            subject.sendMessage("Limited... Try again later")
        }

        limit(
            case("ping2"),
            limitWith(limitPerMinute = 60),
            fallback = fallback
        ) {
            subject.sendMessage("pong2")
        }


        // Do nothing fallback
        limit(
            case("ping3"),
            limitWith(limitPerMinute = 60),
            fallback = {}
        ) {
            subject.sendMessage("pong3")
        }
    }

    bot.run {
        login()
        join()
    }
}