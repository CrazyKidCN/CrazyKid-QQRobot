package cn.crazykid.qqrobot.listener.group.message

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.message.EventGroupMessage
import cc.moecraft.icq.sender.message.MessageBuilder
import cc.moecraft.icq.sender.message.components.ComponentReply
import cn.hutool.core.util.ObjectUtil
import cn.hutool.core.util.RandomUtil
import org.springframework.stereotype.Component
import java.util.stream.Collectors

/**
 * @author CrazyKid
 * @date 2021年11月05日10:21:59
 */
@Component
class GroupMessageRandomPickListener() : IcqListener() {
    @EventHandler
    fun event(event: EventGroupMessage) {
        if (event.message.length > 21 || event.message.contains("[")) {
            return
        }
        if (!event.message.endsWith("?") && !event.message.endsWith("？")) {
            return;
        }

        val message = event.message.replace(Regex("\\s*"), "").replace(Regex("\\pP"), "")

        if (ObjectUtil.isEmpty(message) || !message.contains("还是")) {
            return
        }

        val split = message.split("还是")

        val list = split.stream().filter(ObjectUtil::isNotEmpty).distinct().collect(Collectors.toList())
        if (list.size <= 1) {
            return
        }

        val randomInt = RandomUtil.randomInt(0, list.size)

        val messageBuilder = MessageBuilder()
        messageBuilder.add(ComponentReply(event.messageId))
            .add(list[randomInt]).add("！")

        event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
    }
}
