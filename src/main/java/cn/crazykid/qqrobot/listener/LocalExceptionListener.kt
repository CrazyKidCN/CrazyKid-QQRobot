package cn.crazykid.qqrobot.listener

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.local.EventLocalException

/**
 * @author CrazyKid
 * @date 2021/3/30 10:59
 */
class LocalExceptionListener : IcqListener() {
    @EventHandler
    fun onEvent(event: EventLocalException) {
        event.bot.logger.error(event.exception)
    }
}
