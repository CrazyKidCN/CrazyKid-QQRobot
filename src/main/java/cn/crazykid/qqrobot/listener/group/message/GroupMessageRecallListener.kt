package cn.crazykid.qqrobot.listener.group.message

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.notice.EventNoticeGroupRecall
import cc.moecraft.icq.sender.message.MessageBuilder
import cc.moecraft.icq.sender.message.components.ComponentAt
import cn.crazykid.qqrobot.enum.FeatureEnum
import cn.crazykid.qqrobot.service.IFeatureService
import cn.hutool.core.util.RandomUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * QQ群信息撤回监听, 随机套餐
 *
 * @author CrazyKid (i@crazykid.moe)
 * @since 2020/12/29 22:53
 */
@Component
class GroupMessageRecallListener : IcqListener() {
    @Value("\${group.recall.mute:false}")
    private var isEnable: Boolean = false

    @Autowired
    private lateinit var featureService: IFeatureService

    @EventHandler
    fun groupMessageRecall(event: EventNoticeGroupRecall) {
        event.getBot().getLogger().debug(
            "群 {} 的 {} 撤回了 {} 的消息: {}", event.getGroupId(),
            event.getOperatorId(), event.getUserId(), event.getMessage(true)
        )

        if (!isEnable) {
            return
        }

        if (!featureService.isFeatureEnable(event.groupId, FeatureEnum.RECALL_MUTE)) {
            return
        }

        if (event.isOperatorAdmin()) {
            // 撤回人是管理员, 不响应.
            return
        }

        if (!event.isAdmin()) {
            // 自己不是管理员, 不响应.
            return
        }
        // 撤回随机禁言.
        val minute: Int = RandomUtil.randomInt(1, 60)
        event.getHttpApi().setGroupBan(event.getGroupId(), event.getOperatorId(), minute * 60L)
        val messageBuilder = MessageBuilder()
        messageBuilder.add(ComponentAt(event.getOperatorId()))
            .add("撤回了一条消息并roll到")
            .add(minute)
            .add("分钟禁言套餐")
        event.getHttpApi().sendGroupMsg(event.getGroupId(), messageBuilder.toString())
    }

    // todo 做成配置化
//    init {
//        val setting: Setting = SettingUtil.getRobotSetting()
//        val enable = setting["Base", "RecallMute"]
//        isEnable = StringUtils.isNotBlank(enable) && "0" != enable
//    }
}
