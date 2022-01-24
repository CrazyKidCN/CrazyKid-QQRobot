package cn.crazykid.qqrobot.listener.group.message

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.message.EventGroupMessage
import com.google.common.collect.Maps
import org.springframework.stereotype.Component

/**
 * 群消息计数器 用于排卡功能跟别的bot有功能冲突的场合
 *
 * @author CrazyKid (i@crazykid.moe)
 * @since 2021/9/1 15:17
 */
@Component
class GroupMessageCounterListener : IcqListener() {
    companion object {
        @JvmField
        val GROUP_MAP: MutableMap<Long, MutableMap<Long, Int>> = mutableMapOf()

        @JvmStatic
        fun getMessageCountInGroup(groupId: Long, senderId: Long): Int? {
            if (!GROUP_MAP.containsKey(groupId)) {
                return 0
            }
            val groupMap: Map<Long, Int> = GROUP_MAP[groupId]!!
            return if (!groupMap.containsKey(senderId)) {
                0
            } else groupMap[senderId]
        }
    }

    @EventHandler
    fun event(event: EventGroupMessage) {
        val messageCountMap: MutableMap<Long, Int>
        if (!GROUP_MAP.containsKey(event.groupId)) {
            messageCountMap = Maps.newHashMap()
            GROUP_MAP[event.groupId] = messageCountMap
        } else {
            messageCountMap = GROUP_MAP[event.groupId]!!
        }
        if (!messageCountMap.containsKey(event.senderId)) {
            messageCountMap[event.senderId] = 1
        } else {
            messageCountMap[event.senderId] = messageCountMap[event.senderId]!! + 1
        }
    }
}
