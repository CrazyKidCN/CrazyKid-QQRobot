package cn.crazykid.qqrobot.listener.group.message

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.message.EventGroupMessage
import cc.moecraft.icq.sender.message.MessageBuilder
import cc.moecraft.icq.sender.message.components.ComponentAt
import cc.moecraft.icq.sender.message.components.ComponentReply
import cn.crazykid.qqrobot.enums.FeatureEnum
import cn.crazykid.qqrobot.service.IFeatureService
import cn.hutool.core.lang.WeightRandom.WeightObj
import cn.hutool.core.util.RandomUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

/**
 * QQ群信息监听
 *
 * @author CrazyKid
 * @since 2020/12/27 16:18
 */
@Component
class GroupMessageBotAtListener : IcqListener() {
    @Autowired
    private lateinit var featureService: IFeatureService

    /**
     * At机器人禁言事件
     */
    @EventHandler
    fun event(event: EventGroupMessage) {
        if (!featureService.isFeatureEnable(event.groupId, FeatureEnum.BOT_AT)) {
            return
        }

        // 号主就是牛逼
        if (event.senderId == 694372459L) {
            return
        }
        val matchMessage = ComponentAt(event.selfId).toString()
        if (event.message.contains(matchMessage)) {
            // 统计今日被@次数
            GroupMessageCountListener.incrCountCache(GroupMessageCountListener.REDIS_AT_COUNT_KEY)
            // 提醒万里该学习了
            if (event.senderId == 974060035L) {
                val messageBuilder = MessageBuilder()
                    .add(ComponentReply(event.messageId))
                    .add("张姐，别at了，该学习了\uD83E\uDD75\uD83E\uDD75\uD83E\uDD75")
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
                return
            }
            if (!event.isAdmin && !event.isAdmin(event.senderId)) {
                val messageBuilder = MessageBuilder()
                    .add(ComponentReply(event.messageId))
                    .add("不许@我!! 我要是群管一定口球你!! (ﾟДﾟ*)ﾉ")
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
            } else if (event.isAdmin(event.senderId)) {
                // 不与控制bot撤回功能冲突
                if (event.message.contains("撤回") || event.message.contains("recall")) {
                    return
                }
                val messageBuilder = MessageBuilder()
                    .add(ComponentReply(event.messageId))
                    .add("狗管理, @你🐴@ ヽ(｀⌒´メ)ノ") // ヽ(｀⌒´メ)ノ
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
            } else {
                // 凌晨00:00-5:59，固定禁言到早上6点，晚安套餐
                val timeZone = TimeZone.getTimeZone("Asia/Shanghai")
                val now = Calendar.getInstance()
                now.timeZone = timeZone
                val hour = now[Calendar.HOUR_OF_DAY]
                if (hour < 6) {
                    val target = Calendar.getInstance()
                    target.timeZone = timeZone
                    // 禁言到早上7点
                    target[target[Calendar.YEAR], target[Calendar.MONTH], target[Calendar.DATE], 7] = 0
                    // 计算到当前时间相差的秒数
                    val second = (target.timeInMillis - now.timeInMillis) / 1000
                    event.ban(second)
                    val messageBuilder = MessageBuilder()
                        .add(ComponentAt(event.senderId))
                        .add("晚安")
                    event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
                } else {
                    // 随机禁言
                    val weightRandom = RandomUtil.weightRandom(
                        arrayOf(
                            WeightObj(1, 5.0),
                            WeightObj(2, 10.0),
                            WeightObj(3, 85.0)
                        )
                    )
                    val result = weightRandom.next()
                    val messageBuilder = MessageBuilder()
                        .add(ComponentReply(event.messageId))
                    when (result) {
                        1 -> {
                            val muteMinute = RandomUtil.randomInt(300, 600).toLong()
                            messageBuilder.add("没事别随便@老子，@了老子把你打的满身是屎，老子是你能@的人吗？？？")
                            event.ban(muteMinute * 60)
                        }
                        2 -> messageBuilder.add("BOT酱现在心情不错，就不给你发套餐了( ͡° ͜ʖ ͡°)")
                        3 -> {
                            val muteMinute2 = RandomUtil.randomInt(1, 15).toLong()
                            messageBuilder.add("不许@我 ヽ(｀⌒´メ)ノ!! 吃我" + muteMinute2 + "分钟禁言套餐!")
                            event.ban(muteMinute2 * 60)
                        }
                        else -> {}
                    }
                    event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
                }
            }
        }
    }
}
