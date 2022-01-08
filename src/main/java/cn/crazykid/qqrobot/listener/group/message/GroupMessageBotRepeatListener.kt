package cn.crazykid.qqrobot.listener.group.message

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.message.EventGroupMessage
import cn.hutool.core.util.RandomUtil
import org.springframework.stereotype.Component

/**
 * QQ群信息监听
 *
 * @author CrazyKid
 * @since 2020/12/27 16:18
 */
@Component
class GroupMessageBotRepeatListener : IcqListener() {
    // 重复的消息次数
    private val repeatMessageCountMap: MutableMap<Long, Int> = mutableMapOf()

    // 保存群最后一条消息
    private val repeatMessageMap: MutableMap<Long, String> = mutableMapOf()

    /**
     * 复读事件
     */
    @EventHandler
    fun event(event: EventGroupMessage) {
        if (!repeatMessageMap.containsKey(event.groupId)) {
            repeatMessageMap[event.groupId] = event.rawMessage
            repeatMessageCountMap[event.groupId] = 0
            return
        }
        val lastMessage = repeatMessageMap[event.groupId]
        if (lastMessage == event.rawMessage) {
            // 与上一条消息一致
            repeatMessageCountMap.compute(event.groupId) { _: Long?, v: Int? -> v?.plus(1) }
            val repeatCount = repeatMessageCountMap.getOrDefault(event.groupId, 0)
            if (repeatCount == 2) {
                val i = RandomUtil.randomInt(0, 4)
                if (i == 1) {
                    event.httpApi.sendGroupMsg(event.groupId, "打断复读! （<ゝω・）☆")
                } else {
                    event.httpApi.sendGroupMsg(event.groupId, event.rawMessage)
                    // 记录今日复读数
                    GroupMessageCountListener.incrCountCache(GroupMessageCountListener.REDIS_REPEAT_COUNT_KEY)
                }
            }
        } else {
            // 与上一条消息不一致
            repeatMessageMap[event.groupId] = event.rawMessage
            repeatMessageCountMap[event.groupId] = 0
        }
    }
}
