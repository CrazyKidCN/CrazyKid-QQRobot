package cn.crazykid.qqrobot.listener.group.message

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.message.EventGroupMessage
import cc.moecraft.icq.sender.message.MessageBuilder
import cc.moecraft.icq.sender.message.components.ComponentReply
import org.springframework.stereotype.Component

/**
 * QQ群信息监听
 *
 * @author CrazyKid
 * @since 2022/1/29 14:01
 */
@Component
class GroupMessageHelpListener : IcqListener() {
    @EventHandler
    fun event(event: EventGroupMessage) {
        if (event.message != "帮助" && event.message != "help") {
            return
        }
        val message = MessageBuilder()
            .add(ComponentReply(event.messageId))
            .add("http://showdoc.crazykid.cn/web/#/5/26")
        event.httpApi.sendGroupMsg(event.groupId, message.toString())
    }
}
