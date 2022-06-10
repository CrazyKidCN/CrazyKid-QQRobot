package cn.crazykid.qqrobot.command

import cc.moecraft.icq.command.CommandProperties
import cc.moecraft.icq.command.interfaces.EverywhereCommand
import cc.moecraft.icq.event.events.message.EventGroupMessage
import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.icq.sender.message.MessageBuilder
import cc.moecraft.icq.sender.message.components.ComponentReply
import cc.moecraft.icq.user.User
import cc.moecraft.utils.StringUtils
import cn.crazykid.qqrobot.enums.FeatureEnum
import cn.crazykid.qqrobot.enums.MessageTypeEnum
import cn.crazykid.qqrobot.service.IFeatureService
import cn.hutool.core.util.ObjectUtil
import cn.hutool.core.util.RandomUtil
import com.google.common.collect.Lists
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors

/**
 * !roll 随机数
 *
 * @author CrazyKid
 * @date 2021/1/7 14:13
 */
@Component
class RollCommand : EverywhereCommand {
    @Autowired
    private lateinit var featureService: IFeatureService

    override fun run(event: EventMessage, sender: User, command: String, args: ArrayList<String>): String? {
        val messageBuilder = MessageBuilder()

        if (MessageTypeEnum.GROUP.code == event.messageType) {
            // 如果是群聊信息
            val groupId = (event as EventGroupMessage).groupId
            if (!featureService.isFeatureEnable(groupId, FeatureEnum.ROLL)) {
                return null
            }
            // 以回复的形式响应
            messageBuilder.add(ComponentReply(event.messageId))
        }

        if (ObjectUtil.isEmpty(args)) {
            messageBuilder.add("[Roll随机数] 使用方法:")
                .newLine()
                .add("!roll 最小值 最大值 次数(可选) 是否重复(可选,0=不重复,1=可重复,默认不重复)").newLine()
                .add("使用示例:").newLine()
                .add("!roll 1 10 (roll 1~10的随机数1次)").newLine()
                .add("!roll 1 10 10 (roll 1~10的随机数10次,结果不重复)").newLine()
                .add("!roll 1 10 10 1 (roll 1~10的随机数10次,结果可重复)")
            return messageBuilder.toString()
        } else if (args.size < 2) {
            messageBuilder.add("参数不足。").newLine()
                .add("使用示例:").newLine()
                .add("!roll 1 10 (roll 1~10的随机数1次)").newLine()
                .add("!roll 1 10 10 (roll 1~10的随机数10次,结果不重复)").newLine()
                .add("!roll 1 10 10 1 (roll 1~10的随机数10次,结果可重复)")
            return messageBuilder.toString()
        }
        if (!StringUtils.isNumeric(args[0]) || !StringUtils.isNumeric(args[1])) {
            messageBuilder.add("参数应该为非负数字")
            return messageBuilder.toString()
        }
        val min = args[0].toInt()
        val max = args[1].toInt()
        var rollTime = 1
        var allowRepeat = false
        if (args.size > 2) {
            if (!StringUtils.isNumeric(args[2])) {
                messageBuilder.add("参数应该非负数字")
                return messageBuilder.toString()
            }
            rollTime = args[2].toInt()
        }
        if (args.size > 3) {
            if (!StringUtils.isNumeric(args[3])) {
                messageBuilder.add("参数应该为非负数字")
                return messageBuilder.toString()
            }
            allowRepeat = args[3].toInt() > 0
        }
        if (Math.abs(min) > 100000 || Math.abs(max) > 100000) {
            messageBuilder.add("最小值和最大值不能超过100000")
            return messageBuilder.toString()
        }
        if (max <= min) {
            messageBuilder.add("最大值不能小于最小值或者等于最小值")
            return messageBuilder.toString()
        }
        if (rollTime < 1 || rollTime > 50) {
            messageBuilder.add("只能roll 1-50 次, 太多了我roll不动(")
            return messageBuilder.toString()
        }
        val pool: MutableList<Int> = Lists.newArrayList()
        for (i in min..max) {
            pool.add(i)
        }
        val rollResultList: MutableList<Int> = Lists.newArrayListWithCapacity(rollTime)
        for (i in 0 until rollTime) {
            if (pool.size == 0) {
                break
            }
            val randomIndex = RandomUtil.randomInt(0, pool.size)
            rollResultList.add(pool[randomIndex])
            if (!allowRepeat) {
                pool.removeAt(randomIndex)
            }
        }
        val rollResultStr = rollResultList.stream()
            .sorted()
            .map { obj: Int? -> java.lang.String.valueOf(obj) }
            .collect(Collectors.joining(", "))
        messageBuilder.add("本次roll点结果如下: ")
            .add("(范围").add(min).add("~").add(max)
        if (rollTime > 1) {
            messageBuilder.add(",").add("次数").add(rollTime).add(",")
                .add(if (allowRepeat) "可重复" else "不重复")
        }
        messageBuilder.add(")")
        messageBuilder.newLine().add(rollResultStr)
        return messageBuilder.toString()
    }

    override fun properties(): CommandProperties {
        return CommandProperties("roll")
    }
}
