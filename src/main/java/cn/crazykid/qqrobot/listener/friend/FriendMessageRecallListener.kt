package cn.crazykid.qqrobot.listener.friend

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.notice.EventNoticeFriendRecall
import org.springframework.stereotype.Component

/**
 * @author CrazyKid (i@crazykid.moe)
 * @since 2020/12/30 21:23
 */
@Component
class FriendMessageRecallListener : IcqListener() {
    @EventHandler
    fun friendMessageRecall(event: EventNoticeFriendRecall) {
        event.bot.logger.debug(
            "{} 撤回了消息: {}", event.sender.info.nickname,
            event.getMessage(true)
        )
    }
}
