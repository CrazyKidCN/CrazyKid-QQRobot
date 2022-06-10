package cn.crazykid.qqrobot.listener.group.message

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.message.EventGroupMessage
import cc.moecraft.icq.sender.message.MessageBuilder
import cc.moecraft.icq.sender.message.components.ComponentAt
import cc.moecraft.icq.sender.message.components.ComponentReply
import cn.crazykid.qqrobot.entity.Arcade
import cn.crazykid.qqrobot.entity.ArcadeQueuePlayer
import cn.crazykid.qqrobot.enums.FeatureEnum
import cn.crazykid.qqrobot.exception.OperateFailedException
import cn.crazykid.qqrobot.service.IArcadeQueuePlayerService
import cn.crazykid.qqrobot.service.IArcadeQueueService
import cn.crazykid.qqrobot.service.IFeatureService
import cn.hutool.core.util.ReUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import java.util.regex.Pattern

/**
 * 机厅排卡助手v2
 *
 * @author CrazyKid (i@crazykid.moe)
 * @since 2022/1/9 10:12
 */
@Component
class GroupMessageArcadeQueueListener : IcqListener() {
    @Autowired
    private lateinit var arcadeQueueService: IArcadeQueueService

    @Autowired
    private lateinit var arcadeQueuePlayerService: IArcadeQueuePlayerService

    @Autowired
    private lateinit var featureService: IFeatureService

    @Value("\${arcadeCardCounter.enable:false}")
    private var isEnable: Boolean = false

    private val joinQueuePattern = Pattern.compile("^我到(.*?)了$")
    private val joinQueueGuestPattern = Pattern.compile("^(.*?)有路人$")
    private val finishGamePattern = Pattern.compile("^(.*?)打完了$")
    private val resetPattern = Pattern.compile("^(\\d{1,2})重置$")
    private val leavePattern = Pattern.compile("^(\\d{1,2})?(暂离|不在)$")
    private val backPattern = Pattern.compile("^(.*?)回来了$")
    private val quitQueuePattern = Pattern.compile("^(\\d{1,2})?(退勤|走了)$")
    private val viewQueuePattern = Pattern.compile("^(.*?)队列$")
    private val exchangeQueuePattern = Pattern.compile("^(调序|对调)[\\s+]*(\\d{1,2})[\\s+]*(\\d{1,2})$")

    @EventHandler
    fun event(event: EventGroupMessage) {
        if (!isEnable || !featureService.isFeatureEnable(event.groupId, FeatureEnum.CARD_QUEUE)) {
            return
        }
        if (event.groupId != 604946573L) {
            //bot test group
            //return
        }
        // bot响应的信息
        var messageBuilder = MessageBuilder()
            .add(ComponentReply(event.messageId))

        // 接收到的信息
        val message = event.getMessage().trim()
        // 发送消息的用户名称
        val nickname =
            if (event.groupSender.info.card.isNullOrBlank()) event.groupSender.info.nickname else event.groupSender.info.card
        // 通过正则匹配到的字符串
        var matchMsg: String? = null;

        // 参与队列
        matchMsg = ReUtil.get(joinQueuePattern, message, 1)
        if (matchMsg != null) {
            // 根据机厅名获取机厅 获取不到则终止
            val arcade: Arcade = arcadeQueueService.getArcade(matchMsg, event.groupId) ?: return
            // 该玩家是否已经加入任何一个队列 若已加入则报错
            val playerQueueInfo = arcadeQueuePlayerService.getPlayerQueueInfo(event.senderId)
            if (playerQueueInfo != null) {
                event.httpApi.sendGroupMsg(
                    event.groupId,
                    messageBuilder.add("您已经在 ${playerQueueInfo.arcadeName} 的队列中, 无法重复加入, 如需退出队列请输入\"退勤\"或\"走了\"")
                        .toString()
                )
                return
            }

            val player = ArcadeQueuePlayer()
            player.nickname = nickname
            player.qqNumber = event.senderId
            player.groupNumber = event.groupId
            player.arcadeName = arcade.name
            player.status = 1
            player.joinQueueDate = Date()
            player.keepIndexCount = 0
            player.guest = 0

            val currentQueue = arcadeQueueService.pushPlayerToQueue(event.groupId, arcade.name, event.senderId, player)
            messageBuilder.add("您已加入 ${arcade.name} 的排队队列。").newLine()
                .add("当前队列人数: ${currentQueue.size}").newLine()
                .add("当您上机完毕后请输入\"我打完了\"来回到队列末尾").newLine()
                .add("退出队列请输入\"退勤\"或\"走了\"").newLine()
                .add("更多帮助说明: http://showdoc.crazykid.cn/web/#/5/24").newLine()
                .add("开发中功能, 有bug也请不要意外:)")
            event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())

            val queueMessage = MessageBuilder()
                .add("${arcade.name} 当前队列:").add(this.buildQueueStringMessage(currentQueue, false))
            event.httpApi.sendGroupMsg(event.groupId, queueMessage.toString())
            return
        }

        // 队列添加路人
        matchMsg = ReUtil.get(joinQueueGuestPattern, message, 1)
        if (matchMsg != null) {
            // 根据机厅名获取机厅 获取不到则终止
            val arcade: Arcade = arcadeQueueService.getArcade(matchMsg, event.groupId) ?: return

            val playerQueueInfo = arcadeQueuePlayerService.getPlayerQueueInfo(event.senderId)
            if (playerQueueInfo == null || playerQueueInfo.arcadeName != arcade.name) {
                //event.httpApi.sendGroupMsg(event.groupId, messageBuilder.add("你不在队列里, 无法进行该操作").toString())
                return
            }

            val player = ArcadeQueuePlayer()
            player.nickname = "路人"
            player.qqNumber = System.currentTimeMillis()
            player.groupNumber = event.groupId
            player.arcadeName = arcade.name
            player.status = 1
            player.joinQueueDate = Date()
            player.keepIndexCount = 0
            player.guest = 1

            val currentQueue = arcadeQueueService.pushPlayerToQueue(event.groupId, arcade.name, event.senderId, player)
            messageBuilder.add("已添加一个路人到 ${arcade.name} 队列。").newLine()
                .add("当前队列人数: ${currentQueue.size}").newLine()
                .add("当路人打完后请输入\"<数字>打完了\"").newLine()
                .add("路人走了请输入\"<数字>走了\"").newLine()
                .add("(数字表示路人所在队列位置)")
            event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
            return
        }

        // x打完了
        matchMsg = ReUtil.get(finishGamePattern, message, 1)
        if (matchMsg != null) {
            if (matchMsg != "我" && !this.isNumeric(matchMsg)) {
                return
            }
            val playerQueueInfo = arcadeQueuePlayerService.getPlayerQueueInfo(event.senderId)
            if (playerQueueInfo == null) {
                //event.httpApi.sendGroupMsg(event.groupId, messageBuilder.add("你不在队列里, 无法进行该操作").toString())
                return
            }

            try {
                val currentQueueList = arcadeQueueService.resetPlayerToQueueLast(
                    matchMsg,
                    event.senderId,
                    playerQueueInfo.groupNumber,
                    playerQueueInfo.arcadeName,
                    true
                )
                messageBuilder.add("已将").add(if (matchMsg == "我") "您" else "${matchMsg}号位").add("移到队列末尾。");
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())

                messageBuilder = MessageBuilder().add("当前队列:").add(this.buildQueueStringMessage(currentQueueList, true))
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
            } catch (e: OperateFailedException) {
                if (e.sendMessage) {
                    event.httpApi.sendGroupMsg(event.groupId, e.message)
                }
            }
            return
        }

        // x重置
        matchMsg = ReUtil.get(resetPattern, message, 1)
        if (matchMsg != null) {
            if (!this.isNumeric(matchMsg)) {
                return
            }
            val playerQueueInfo = arcadeQueuePlayerService.getPlayerQueueInfo(event.senderId)
            if (playerQueueInfo == null) {
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.add("你不在队列里, 无法进行该操作").toString())
                return
            }

            var position = matchMsg.toInt();

            val currentQueueList = arcadeQueueService.getQueue(playerQueueInfo.groupNumber, playerQueueInfo.arcadeName)
            if (position > currentQueueList.size) {
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.add("位置超出队列长度").toString())
                return
            }

            position--;
            val resetPlayer = currentQueueList[position]

            try {
                val afterQueueList = arcadeQueueService.resetPlayerToQueueLast(
                    matchMsg,
                    event.senderId,
                    playerQueueInfo.groupNumber,
                    playerQueueInfo.arcadeName,
                    false
                )
                messageBuilder.add("已将 ")
                messageBuilder.add(
                    this.buildPlayerName(
                        resetPlayer.qqNumber,
                        resetPlayer.nickname,
                        event.senderId,
                        resetPlayer.guest,
                        true
                    )
                )
                messageBuilder.add(" 重置到队列末尾。");
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())

                messageBuilder = MessageBuilder().add("当前队列:").add(this.buildQueueStringMessage(afterQueueList, true))
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
            } catch (e: OperateFailedException) {
                if (e.sendMessage) {
                    event.httpApi.sendGroupMsg(event.groupId, e.message)
                }
            }
            return
        }

        // x暂离
        var matchGroups = ReUtil.getAllGroups(leavePattern, message)
        if (matchGroups != null && matchGroups.isNotEmpty()) {
            var self = false
            matchMsg = matchGroups[1]
            if (matchMsg.isNullOrBlank()) {
                // 自己暂离
                self = true
            }
            if (!self && !this.isNumeric(matchMsg)) {
                return
            }
            val playerQueueInfo = arcadeQueuePlayerService.getPlayerQueueInfo(event.senderId)
            if (playerQueueInfo == null) {
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.add("你不在队列里, 无法进行该操作").toString())
                return
            }
            // 获取当前队列
            val currentQueueList = arcadeQueueService.getQueue(playerQueueInfo.groupNumber, playerQueueInfo.arcadeName)

            var position: Int? = null
            if (self) {
                // 获取自己在队列中的位置
                currentQueueList.forEachIndexed { index, player ->
                    if (player.qqNumber.equals(event.senderId)) {
                        position = index
                        return@forEachIndexed
                    }
                }
            } else {
                position = matchMsg.toInt()
                if (position!! > currentQueueList.size) {
                    event.httpApi.sendGroupMsg(event.groupId, messageBuilder.add("位置超出队列长度").toString())
                    return
                }
                position = position!! - 1;
            }
            if (position == null) {
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.add("发生错误: 没有在队列中找到您").toString())
                return
            }

            val leavePlayer = currentQueueList[position!!]

            val afterQueueList = arcadeQueueService.markPlayerStatus(
                position!!,
                event.senderId,
                playerQueueInfo.groupNumber,
                playerQueueInfo.arcadeName,
                2
            )
            messageBuilder.add("已将")
            messageBuilder.add(
                this.buildPlayerName(
                    leavePlayer.qqNumber,
                    leavePlayer.nickname,
                    event.senderId,
                    leavePlayer.guest,
                    true
                )
            )
            messageBuilder.add("标记为暂离。")
            event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
            return
        }

        // x回来了
        matchMsg = ReUtil.get(backPattern, message, 1)
        if (!matchMsg.isNullOrBlank()) {
            var self = false
            if (matchMsg == "我") {
                // 自己回来了
                self = true
            }
            if (!self && !this.isNumeric(matchMsg)) {
                return
            }
            val playerQueueInfo = arcadeQueuePlayerService.getPlayerQueueInfo(event.senderId)
            if (playerQueueInfo == null) {
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.add("你不在队列里, 无法进行该操作").toString())
                return
            }
            // 获取当前队列
            val currentQueueList = arcadeQueueService.getQueue(playerQueueInfo.groupNumber, playerQueueInfo.arcadeName)

            var position: Int? = null
            if (self) {
                // 获取自己在队列中的位置
                currentQueueList.forEachIndexed { index, player ->
                    if (player.qqNumber.equals(event.senderId)) {
                        position = index
                        return@forEachIndexed
                    }
                }
            } else {
                position = matchMsg.toInt()
                if (position!! > currentQueueList.size) {
                    event.httpApi.sendGroupMsg(event.groupId, messageBuilder.add("位置超出队列长度").toString())
                    return
                }
                position = position!! - 1;
            }
            if (position == null) {
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.add("发生错误: 没有在队列中找到您").toString())
                return
            }

            val backPlayer = currentQueueList[position!!]

            val afterQueueList = arcadeQueueService.markPlayerStatus(
                position!!,
                event.senderId,
                playerQueueInfo.groupNumber,
                playerQueueInfo.arcadeName,
                1
            )
            messageBuilder.add("已将")
            messageBuilder.add(
                this.buildPlayerName(
                    backPlayer.qqNumber,
                    backPlayer.nickname,
                    event.senderId,
                    backPlayer.guest,
                    true
                )
            )
            messageBuilder.add("取消暂离。")
            event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
            return
        }

        // 退勤
        matchGroups = ReUtil.getAllGroups(quitQueuePattern, message)
        if (matchGroups != null && matchGroups.isNotEmpty()) {
            var self = false
            matchMsg = matchGroups[1]
            if (matchMsg.isNullOrBlank()) {
                self = true
            }
            if (!self && !this.isNumeric(matchMsg)) {
                return
            }
            val playerQueueInfo = arcadeQueuePlayerService.getPlayerQueueInfo(event.senderId)
            if (playerQueueInfo == null) {
                //event.httpApi.sendGroupMsg(event.groupId, messageBuilder.add("你不在队列里, 无法进行该操作").toString())
                return
            }
            // 获取当前队列
            val currentQueueList = arcadeQueueService.getQueue(playerQueueInfo.groupNumber, playerQueueInfo.arcadeName)

            var position: Int? = null
            if (self) {
                // 获取自己在队列中的位置
                currentQueueList.forEachIndexed { index, player ->
                    if (player.qqNumber.equals(event.senderId)) {
                        position = index
                        return@forEachIndexed
                    }
                }
            } else {
                position = matchMsg.toInt()
                if (position!! > currentQueueList.size) {
                    event.httpApi.sendGroupMsg(event.groupId, messageBuilder.add("位置超出队列长度").toString())
                    return
                }
                position = position!! - 1;
            }
            if (position == null) {
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.add("发生错误: 没有在队列中找到您").toString())
                return
            }

            val quitPlayer = currentQueueList[position!!]

            val afterQueueList = arcadeQueueService.removePlayerInQueue(
                position!!,
                event.senderId,
                playerQueueInfo.groupNumber,
                playerQueueInfo.arcadeName
            )
            messageBuilder.add(
                this.buildPlayerName(
                    quitPlayer.qqNumber,
                    quitPlayer.nickname,
                    event.senderId,
                    quitPlayer.guest,
                    true
                )
            )
            messageBuilder.add("已退出队列。").newLine()
                .add(playerQueueInfo.arcadeName).add(" 当前队列人数: ").add(afterQueueList.size)
            event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
            return
        }

        // 机厅有谁
        matchMsg = ReUtil.get(viewQueuePattern, message, 1)
        if (matchMsg != null) {
            // 根据机厅名获取机厅 获取不到则终止
            val arcade: Arcade = arcadeQueueService.getArcade(matchMsg, event.groupId) ?: return

            // 获取当前队列
            val currentQueue = arcadeQueueService.getQueue(event.groupId, arcade.name)

            val queueMessage = MessageBuilder()
                .add("${arcade.name} 当前队列:").add(this.buildQueueStringMessage(currentQueue, false))
            event.httpApi.sendGroupMsg(event.groupId, queueMessage.toString())
            return
        }

        // 调序
        matchGroups = ReUtil.getAllGroups(exchangeQueuePattern, message)
        if (matchGroups != null && matchGroups.isNotEmpty()) {
            var pos1 = matchGroups[2].toInt()
            var pos2 = matchGroups[3].toInt()

            val playerQueueInfo = arcadeQueuePlayerService.getPlayerQueueInfo(event.senderId)
            if (playerQueueInfo == null) {
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.add("你不在队列里, 无法进行该操作").toString())
                return
            }

            // 获取当前队列
            val currentQueueList = arcadeQueueService.getQueue(playerQueueInfo.groupNumber, playerQueueInfo.arcadeName)

            if (pos1 > currentQueueList.size || pos2 > currentQueueList.size) {
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.add("位置超出队列长度").toString())
                return
            }
            pos1--
            pos2--

            val player1 = currentQueueList[pos1]
            val player2 = currentQueueList[pos2]

            val afterQueueList = arcadeQueueService.swapPlayerInQueue(
                pos1,
                pos2,
                event.senderId,
                playerQueueInfo.groupNumber,
                playerQueueInfo.arcadeName
            )

            messageBuilder.add("已将")
                .add(this.buildPlayerName(player1.qqNumber, player1.nickname, event.senderId, player1.guest, true))
                .add("和")
                .add(this.buildPlayerName(player2.qqNumber, player2.nickname, event.senderId, player2.guest, true))
                .add("交换位置。")
            event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())

            messageBuilder = MessageBuilder().add("当前队列:").add(this.buildQueueStringMessage(afterQueueList, false))
            event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
            return
        }

        if (message == "撤销" || message == "撤回") {
            val playerQueueInfo = arcadeQueuePlayerService.getPlayerQueueInfo(event.senderId)
            if (playerQueueInfo == null) {
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.add("你不在队列里, 无法进行该操作").toString())
                return
            }

            try {
                val afterRecallQueue = arcadeQueueService.recallOperate(
                    event.senderId,
                    event.groupId,
                    playerQueueInfo.arcadeName,
                    event.isAdmin(event.senderId),
                    messageBuilder
                )
                messageBuilder.add("已撤销刚才的操作。")
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())

                messageBuilder = MessageBuilder()
                    .add("${playerQueueInfo.arcadeName} 当前队列:")
                    .add(this.buildQueueStringMessage(afterRecallQueue, false))
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
            } catch (e: OperateFailedException) {
                if (e.sendMessage) {
                    event.httpApi.sendGroupMsg(event.groupId, e.message)
                }
            }
            return
        }
    }


    /**
     * 判断字符串是否是正整数
     */
    private fun isNumeric(str: String): Boolean {
        for (c in str) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 构造队列信息
     */
    private fun buildQueueStringMessage(queue: MutableList<ArcadeQueuePlayer>, at: Boolean): String {
        val message = MessageBuilder()
        if (queue.isEmpty()) {
            return message.add("没人, 速度霸机").toString()
        }

        var index = 1
        var atBeforeIndex = 2 // 默认at队列前两名的人, 如果前面有暂离的人, 那么顺延at
        for (player in queue) {
            message.newLine()
            message.add("${index}. ")
            if (index <= atBeforeIndex && player.guest == 0 && at) {
                message.add(ComponentAt(player.qqNumber))
            } else {
                message.add(player.nickname)
            }
            if (player.status == 2) {
                if (player.keepIndexCount == 0) {
                    message.add(" (暂离)")
                } else {
                    message.add(" (暂离 已等待${player.keepIndexCount}回)")
                }
                atBeforeIndex++
            } else {
                if (player.keepIndexCount > 0) {
                    message.add(" (已等待${player.keepIndexCount}回)")
                }
            }
            index++
        }
        return message.toString()
    }

    private fun buildPlayerName(qqNumber: Long, qqName: String, senderId: Long, guest: Int, at: Boolean): String {
        if (qqNumber.equals(senderId)) {
            return "您"
        }
        return if (at && guest == 0) {
            " ${ComponentAt(qqNumber)} "
        } else {
            " $qqName "
        }
    }
}

fun main() {
    val joinQueuePattern = Pattern.compile("^我到(.*?)了$")
    val joinQueueGuestPattern = Pattern.compile("^(.*?)有路人$")
    val finishGamePattern = Pattern.compile("^(.*?)打完了$")
    val resetPattern = Pattern.compile("^(\\d{1,2})重置$")
    val leavePattern = Pattern.compile("^(\\d{1,2})?(暂离|不在)$")
    val backPattern = Pattern.compile("^(.*?)回来了$")
    val quitQueuePattern = Pattern.compile("^(\\d{1,2})?(退勤|走了)$")
    val viewQueuePattern = Pattern.compile("^(.*?)(有谁|队列)$")
    val exchangeQueuePattern = Pattern.compile("^(调序|对调)[\\s+]*(\\d{1,2})[\\s+]*(\\d{1,2})$")

    /**
    [我到优了, 优]
    优
    [优有路人, 优]
    [我打完了, 我]
    [1打完了, 1]
    []
    [1重置, 1]
    [暂离, null, 暂离]
    [1暂离, 1, 暂离]
    [我回来了, 我]
    [1回来了, 1]
    [退勤, null, 退勤]
    [2退勤, 2, 退勤]
    [优有谁, 优, 有谁]
    [对调 1 20, 对调, 1, 20]
    [对调   1     2, 对调, 1, 2]
     */
    println(ReUtil.getAllGroups(joinQueuePattern, "我到优了"))//ok
    println(ReUtil.get(joinQueuePattern, "我到优了", 1))
    println(ReUtil.getAllGroups(joinQueueGuestPattern, "优有路人"))//ok
    println(ReUtil.getAllGroups(finishGamePattern, "我打完了"))//ok
    println(ReUtil.getAllGroups(finishGamePattern, "1打完了"))//ok
    println(ReUtil.getAllGroups(resetPattern, "重置"))//ok
    println(ReUtil.getAllGroups(resetPattern, "1重置"))//ok
    println(ReUtil.getAllGroups(leavePattern, "暂离"))//ok
    println(ReUtil.getAllGroups(leavePattern, "1暂离"))//ok
    println(ReUtil.getAllGroups(backPattern, "我回来了"))//ok
    println(ReUtil.getAllGroups(backPattern, "1回来了"))//ok
    println(ReUtil.getAllGroups(quitQueuePattern, "退勤"))//ok
    println(ReUtil.getAllGroups(quitQueuePattern, "2退勤"))//ok
    println(ReUtil.getAllGroups(viewQueuePattern, "优有谁"))//ok
    println(ReUtil.getAllGroups(exchangeQueuePattern, "对调 1 20"))//ok
    println(ReUtil.getAllGroups(exchangeQueuePattern, "对调   1     2"))//ok
}
