package cn.crazykid.qqrobot.listener.group

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.notice.EventNoticeGroupLuckyKing
import cc.moecraft.icq.sender.message.MessageBuilder
import cc.moecraft.icq.sender.message.components.ComponentAt
import org.springframework.stereotype.Component

/**
 * 群红包监听
 *
 * @author CrazyKid
 * @date 2021/3/31 10:08
 */
@Component
class GroupLuckyKingListener : IcqListener() {
    @EventHandler
    fun event(event: EventNoticeGroupLuckyKing) {
        event.httpApi.sendGroupMsg(
            event.groupId,
            MessageBuilder().add(ComponentAt(event.targetId)).add(" 这不给我来点?").toString()
        )
    }
}
