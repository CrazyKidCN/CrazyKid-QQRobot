package cn.crazykid.qqrobot.listener

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.local.EventLocalException
import org.springframework.stereotype.Component

/**
 * @author CrazyKid
 * @date 2021/3/30 10:59
 */
@Component
class LocalExceptionListener : IcqListener() {
    @EventHandler
    fun onEvent(event: EventLocalException) {
        event.bot.logger.error(event.exception)
    }
}
