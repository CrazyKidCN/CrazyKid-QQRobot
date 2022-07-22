package cn.crazykid.qqrobot.listener.group.message

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.message.EventGroupMessage
import cn.hutool.core.util.ReUtil
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
        // key: 群号 value: map<发送人QQ号, 消息个数>
        @JvmField
        val GROUP_MAP: MutableMap<Long, MutableMap<Long, Int>> = mutableMapOf()

        // key: 群号 value: map<被at人QQ号, 被at消息个数>
        @JvmField
        val GROUP_AT_MAP: MutableMap<Long, MutableMap<Long, Int>> = mutableMapOf()

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

        @JvmStatic
        fun getMessageAtCountInGroup(groupId: Long, atQQ: Long): Int? {
            if (!GROUP_AT_MAP.containsKey(groupId)) {
                return 0
            }
            val groupMap: Map<Long, Int> = GROUP_AT_MAP[groupId]!!
            return if (!groupMap.containsKey(atQQ)) {
                0
            } else groupMap[atQQ]
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

        // 基于hoshino bot的机器人排卡功能回复的消息特征
        if (event.message.startsWith("[CQ:at,qq=") &&
            !event.message.contains("[CQ:reply,id=") &&
            (event.message.contains("现在有") || event.message.contains("更新"))
        ) {
            val atQQ = ReUtil.get("\\[CQ:at,qq=(.*?)\\]", event.getMessage(), 1).toLong()
            val atMessageCountMap: MutableMap<Long, Int>
            if (!GROUP_AT_MAP.containsKey(event.groupId)) {
                atMessageCountMap = Maps.newHashMap()
                GROUP_AT_MAP[event.groupId] = atMessageCountMap
            } else {
                atMessageCountMap = GROUP_AT_MAP[event.groupId]!!
            }
            if (!atMessageCountMap.containsKey(atQQ)) {
                atMessageCountMap[atQQ] = 1
            } else {
                atMessageCountMap[atQQ] = atMessageCountMap[atQQ]!! + 1
            }
        }
    }
}
