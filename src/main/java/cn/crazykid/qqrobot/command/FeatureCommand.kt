package cn.crazykid.qqrobot.command

import cc.moecraft.icq.command.CommandProperties
import cc.moecraft.icq.command.interfaces.GroupCommand
import cc.moecraft.icq.event.events.message.EventGroupMessage
import cc.moecraft.icq.sender.message.MessageBuilder
import cc.moecraft.icq.sender.message.components.ComponentReply
import cc.moecraft.icq.user.Group
import cc.moecraft.icq.user.GroupUser
import cn.crazykid.qqrobot.exception.OperateFailedException
import cn.crazykid.qqrobot.service.IFeatureService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * @author CrazyKid
 * @date 2022/7/27 09:32
 */
@Component
class FeatureCommand : GroupCommand {

    @Autowired
    private lateinit var featureService: IFeatureService

    override fun groupMessage(
        event: EventGroupMessage,
        sender: GroupUser,
        group: Group,
        command: String,
        args: ArrayList<String>
    ): String {
        val messageBuilder = MessageBuilder()
        messageBuilder.add(ComponentReply(event.messageId))

        if (!event.isAdmin(event.senderId) && event.senderId != 694372459L) {
            messageBuilder.add("此命令仅群管或bot主可用")
            return messageBuilder.toString()
        }

        val groupFeatureConfigList = featureService.groupFeatureConfigList(event.groupId)
        if (args.isEmpty()) {
            // 功能列表
            messageBuilder.add("功能编号 | 功能名称 | 开关状态: ")
            for (feature in groupFeatureConfigList) {
                val status = if (feature.enable == 0) "OFF" else "ON"
                messageBuilder.newLine()
                    .add("${feature.id} | ${feature.title} | $status")
            }
            messageBuilder.newLine()
                .add("输入 !feature 功能编号 来查看功能详情").newLine()
                .add("输入 !feature 功能编号 on/off 来开关功能")
            return messageBuilder.toString()
        }

        val featureIndexStr = args[0]
        val featureIndex: Int

        try {
            featureIndex = featureIndexStr.toInt()
        } catch (e: Exception) {
            messageBuilder.add("参数1不合法, 必须是数字")
            return messageBuilder.toString()
        }

        val feature = groupFeatureConfigList.firstOrNull { it.id == featureIndex }
        if (feature == null) {
            messageBuilder.add("功能编号无效")
            return messageBuilder.toString()
        }

        // 查看功能介绍
        if (args.size == 1) {
            messageBuilder.add("功能编号: ${feature.id}").newLine()
                .add("功能标题: ${feature.title}").newLine()
                .add("功能描述: ${if (feature.desc.isNullOrBlank()) "无" else feature.desc}").newLine()
                .add("开关状态: ${if (feature.enable == 0) "OFF" else "ON"}").newLine()
                .add("输入 !feature ${feature.id} on/off 来开启或关闭该功能")
            return messageBuilder.toString()
        }

        if (args.size > 2) {
            messageBuilder.add("参数过多。").newLine()
                .add("命令格式: !feature 功能编号 on/off").newLine()
                .add("例: !feature ${groupFeatureConfigList[0].id} off 表示关闭${groupFeatureConfigList[0].title}")
            return messageBuilder.toString()
        }


        val featureStatusStr = args[1]

        val status: Int = if ("on".equals(featureStatusStr, true)) {
            1
        } else if ("off".equals(featureStatusStr, true)) {
            0
        } else {
            messageBuilder.add("参数2不合法, 必须是on或者off")
            return messageBuilder.toString()
        }

        try {
            featureService.updateGroupFeatureConfig(event.groupId, feature.id!!, status)
        } catch (e: OperateFailedException) {
            if (e.sendMessage) {
                messageBuilder.add(e.message)
                return messageBuilder.toString()
            }
        }
        messageBuilder.add("操作成功!")
        return messageBuilder.toString()
    }

    override fun properties(): CommandProperties {
        return CommandProperties("feature")
    }
}
