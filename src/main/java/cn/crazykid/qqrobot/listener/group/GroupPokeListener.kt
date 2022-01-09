package cn.crazykid.qqrobot.listener.group

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.notice.EventNoticeGroupPoke
import cc.moecraft.icq.sender.message.MessageBuilder
import cc.moecraft.icq.sender.message.components.ComponentAt
import cn.crazykid.qqrobot.listener.group.message.GroupMessageCountListener
import cn.hutool.core.util.RandomUtil
import org.springframework.stereotype.Component

/**
 * 群戳一戳事件监听
 *
 * @author CrazyKid (i@crazykid.moe)
 * @since 2020/12/30 22:04
 */
@Component
class GroupPokeListener : IcqListener() {
    @EventHandler
    fun groupPoke(event: EventNoticeGroupPoke) {
        if (event.targetId != event.selfId) {
            // 戳的不是bot, 不响应.
            return
        }
        // 卍里的特殊待遇, 喝喝
        if (event.userId == 974060035L) {
            val messageBuilder = MessageBuilder()
                .add(ComponentAt(event.userId))
                .add("张姐，别戳了，该学习了\uD83E\uDD75\uD83E\uDD75\uD83E\uDD75")
            event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
            return
        }

        // 统计今日被戳次数
        GroupMessageCountListener.incrCountCache(GroupMessageCountListener.REDIS_POKE_COUNT_KEY)
        if (event.senderGroupUser.isAdmin) {
            // 戳人者是管理员
            val messageBuilder = MessageBuilder()
            messageBuilder.add(ComponentAt(event.userId))
                .add("狗管理, 戳你🐴戳 (╯‵□′)╯")
            event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
            return
        }
        if (!event.targetGroupUser.isAdmin) {
            // bot不是管理员
            val messageBuilder = MessageBuilder()
            messageBuilder.add(ComponentAt(event.userId))
                .add("你再戳! 我要是群管一定把你给禁了 ╰(‵□′)╯")
            event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
            return
        }
        // 戳bot随机禁言.
        val minute = RandomUtil.randomInt(1, 181)
        event.httpApi.setGroupBan(event.groupId, event.userId, minute * 60L)
        val messageBuilder = MessageBuilder()
        messageBuilder.add(ComponentAt(event.userId))
            .add("不许戳我!! 吃我")
            .add(minute)
            .add("分钟禁言套餐! (╯‵□′)╯")
        event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
    }
}
