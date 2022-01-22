package cn.crazykid.qqrobot.listener.group.message

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.message.EventGroupMessage
import cc.moecraft.icq.sender.message.MessageBuilder
import cc.moecraft.icq.sender.message.components.ComponentAt
import org.springframework.stereotype.Component

/**
 * 群红包监听
 *
 * @author CrazyKid
 * @date 2021/3/31 10:26
 */
@Component
class GroupMessageRedPackListener : IcqListener() {
    @EventHandler
    fun event(event: EventGroupMessage) {
        if (!event.getMessage().contains("[CQ:redbag,title=")) {
            return
        }
        var title = event.getMessage().replace("[CQ:redbag,title=", "")
        title = title.substring(0, title.length - 1)

        val m = MessageBuilder()
        m.add(ComponentAt(event.senderId)).add(" 我的呢我的呢?")
        event.httpApi.sendGroupMsg(event.groupId, m.toString())
    }
}
