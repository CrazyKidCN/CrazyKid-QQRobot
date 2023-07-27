package cn.crazykid.qqrobot.listener.friend.message

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.message.EventPrivateMessage
import org.springframework.stereotype.Component

@Component
class FriendMessageListener : IcqListener() {
    @EventHandler
    fun onPrivateMessageEvent(event: EventPrivateMessage) {
        // 记录日志
        event.bot.logger.log("接收到私聊信息(QQ:" + event.sender.id + "): " + event.getMessage())
        // 通知号主
        //event.getHttpApi().sendPrivateMsg(694372459L, event.getSender().getInfo().getNickname() +
        //        "(" +  event.getSenderId() + "): " + event.getRawMessage());

        if (event.message == "在吗") {
            event.httpApi.sendPrivateMsg(event.sender.id, "1")
        }
    }
}
