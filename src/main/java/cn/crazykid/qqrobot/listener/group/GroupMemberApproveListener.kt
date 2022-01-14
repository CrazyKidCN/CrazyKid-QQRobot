package cn.crazykid.qqrobot.listener.group

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.notice.groupmember.increase.EventNoticeGroupMemberApprove
import cc.moecraft.icq.sender.message.MessageBuilder
import org.springframework.stereotype.Component

/**
 * 群员被同意进群事件监听
 *
 * @author CrazyKid
 * @date 2021/1/4 10:25
 */
@Component
class GroupMemberApproveListener : IcqListener() {
    @EventHandler
    fun onEvent(event: EventNoticeGroupMemberApprove) {
        // bot自己进群, 刷新群缓存
        if (event.userId == event.getSelfId()) {
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            event.bot.logger.log("bot自己进了群, 刷新群缓存...")
            event.bot.accountManager.refreshCache()
            return
        }
        val messageBuilder = MessageBuilder()
        //messageBuilder.add("欢迎 ").add(new ComponentAt(event.getUserId())).add(" 进群~");
        val memberCount: Int = event.httpApi.getGroupInfo(event.groupId, true).data.memberCount
        messageBuilder.add("群地位-" + (memberCount - 1))
        event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
    }
}
