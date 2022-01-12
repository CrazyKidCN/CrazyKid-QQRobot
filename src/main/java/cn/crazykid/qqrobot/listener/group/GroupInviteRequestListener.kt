package cn.crazykid.qqrobot.listener.group

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.request.EventGroupInviteRequest
import cc.moecraft.icq.sender.message.MessageBuilder
import org.springframework.stereotype.Component

/**
 * 邀请bot进群事件监听
 *
 * @author CrazyKid
 * @date 2021/1/7 10:35
 */
@Component
class GroupInviteRequestListener : IcqListener() {
    @EventHandler
    fun onEvent(event: EventGroupInviteRequest) {
        // 号主邀请, 自动同意进群
        if (event.userId == 694372459L) {
            event.httpApi.approveGroupRequest(event.flag, event.subType)
        } else {
            val messageBuilder = MessageBuilder()
            messageBuilder.add("${event.userId}邀请bot加入群${event.groupId},已拒绝")
            event.httpApi.sendPrivateMsg(694372459L, messageBuilder.toString())
            event.httpApi.rejectGroupRequest(event.flag, event.subType, "邀请入群请先联系bot主人")
        }
    }
}
