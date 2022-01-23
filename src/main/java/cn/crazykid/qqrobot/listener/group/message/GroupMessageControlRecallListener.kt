package cn.crazykid.qqrobot.listener.group.message

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.message.EventGroupMessage
import cc.moecraft.icq.sender.message.components.ComponentAt
import cn.hutool.core.util.ReUtil
import org.springframework.stereotype.Component

/**
 * QQ群信息监听
 *
 * @author CrazyKid
 * @since 2020/12/27 16:18
 */
@Component
class GroupMessageControlRecallListener : IcqListener() {
    /**
     * 控制bot撤回自己的信息
     * 用法: 回复bot发送的信息, 并带有 @bot + "撤回" 或 "recall" 关键字
     */
    @EventHandler
    fun event(event: EventGroupMessage) {
        // 不是群管或者不是号主不能使用
        if (!event.isAdmin(event.senderId) && event.senderId != 694372459L) {
            return
        }
        val atMessage = ComponentAt(event.getSelfId()).toString()
        event.bot.logger.debug("控制撤回消息事件,收到消息: {}", event.getMessage())
        if (event.getMessage().contains(atMessage) && event.getMessage().contains("[CQ:reply") &&
            (event.getMessage().contains("撤回") || event.getMessage().contains("recall"))
        ) {
            val replyMessageId = ReUtil.get("\\[CQ:reply,id=(.*?)\\]", event.getMessage(), 1).toLong()
            event.httpApi.deleteMsg(replyMessageId)
            event.bot.logger.debug("撤回消息id: $replyMessageId")
        }
    }
}
