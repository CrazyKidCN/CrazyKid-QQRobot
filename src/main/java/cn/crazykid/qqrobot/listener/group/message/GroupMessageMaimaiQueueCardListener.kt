package cn.crazykid.qqrobot.listener.group.message

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.message.EventGroupMessage
import cc.moecraft.icq.sender.message.MessageBuilder
import cc.moecraft.icq.sender.message.components.ComponentReply
import cc.moecraft.icq.user.GroupUser
import cn.crazykid.qqrobot.dao.intf.ArcadeDao
import cn.crazykid.qqrobot.entity.Arcade
import cn.crazykid.qqrobot.enums.FeatureEnum
import cn.crazykid.qqrobot.listener.group.message.GroupMessageCounterListener.Companion.getMessageAtCountInGroup
import cn.crazykid.qqrobot.listener.group.message.GroupMessageCounterListener.Companion.getMessageCountInGroup
import cn.crazykid.qqrobot.service.IFeatureService
import cn.crazykid.qqrobot.util.ArcadeQueueCardUtil
import cn.hutool.core.date.DateUtil
import cn.hutool.core.lang.Console
import cn.hutool.core.thread.ThreadUtil
import cn.hutool.core.util.ReUtil
import com.alibaba.fastjson.JSON
import lombok.SneakyThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import redis.clients.jedis.Jedis
import java.util.*
import java.util.regex.Pattern
import kotlin.math.ceil
import kotlin.math.floor

/**
 * maimai机厅几卡
 *
 * @author CrazyKid (i@crazykid.moe)
 * @since 2021/3/6 17:00
 */
@Component
class GroupMessageMaimaiQueueCardListener : IcqListener() {
    @Autowired
    private lateinit var arcadeDao: ArcadeDao

    @Value("\${arcadeCardCounter.enable:false}")
    private var isEnable: Boolean = false

    @Autowired
    private lateinit var jedis: Jedis

    @Autowired
    private lateinit var featureService: IFeatureService

    private val selectCardNumPattern = Pattern.compile("^(.*?)(现在)?(几|多少)([个张位])?([卡人神爷爹])[?？]?$")
    private val selectCardNumPattern2 = Pattern.compile("^(.*)[jJ几][kK卡]?$")
    private val operateCardNumPattern =
        Pattern.compile("^(.*)([+＋加\\-－减=＝])(\\d{1,6}|[零一两俩二三仨四五六七八九十+＋\\-－])([个张位])?([卡人神爷爹])?[\\s+]*(\\[CQ:at,qq=(\\d{1,12})\\])?$")
    private val whoPattern = Pattern.compile("^(.*)有谁[?？]?$")
    private val wherePattern = Pattern.compile("^(.*)在哪[?？]?$")
    private val markClosePattern = Pattern.compile("^(.*)(停业|开业)$")

    companion object {
        private const val CACHE_NAME = "ArcadeCardQueue_NEW"
        private const val HISTORY_CACHE_NAME = "ArcadeCardQueueOperateHistory"
    }

    @SneakyThrows
    private fun getArcadeList(groupNumber: Long, isReload: Boolean): List<Arcade> {
        Console.log("获取机厅列表...")
        val arcadeList: List<Arcade>
        val json: String? = jedis[CACHE_NAME]
        if (json.isNullOrBlank()) {
            Console.log("从db获取..")
            arcadeList = arcadeDao.selectEnableArcades()
            for (arcade in arcadeList) {
                if (!isReload) {
                    arcade.cardNum = 0
                    arcade.cardUpdateBy = null
                    arcade.cardUpdateTime = null
                }
                Console.log("载入机厅 {}", arcade.name)
            }
            jedis.setex(CACHE_NAME, cacheExpireSecond, JSON.toJSONString(arcadeList))
        } else {
            Console.log("从redis中获取..")
            arcadeList = JSON.parseArray(json, Arcade::class.java)
        }

        // 排序按群号置顶
        // https://www.cnblogs.com/firstdream/p/7204067.html
        arcadeList.sortWith(
            Comparator.comparing(
                { arcade: Arcade -> getArcadeGroupNumber(arcade) }) { x: List<Long>, y: List<Long> ->
                if (x.isEmpty() && y.isEmpty()) {
                    return@comparing 0
                }
                if (x.isEmpty()) {
                    return@comparing -1
                }
                if (y.isEmpty()) {
                    return@comparing 1
                }
                val xnumber = x[0]
                val ynumber = y[0]
                if (xnumber == groupNumber && ynumber != groupNumber) {
                    return@comparing -1
                }
                if (xnumber != groupNumber && ynumber == groupNumber) {
                    return@comparing 1
                }
                0
            })
        return arcadeList
    }

    private fun saveHistory(arcadeName: String, messageParam: String) {
        val json: String? = jedis.hget(HISTORY_CACHE_NAME, arcadeName)
        val historyList: MutableList<String> = if (!json.isNullOrBlank()) {
            JSON.parseArray(json, String::class.java)
        } else {
            mutableListOf()
        }
        var message = messageParam
        message = DateUtil.format(Date(), "HH:mm:ss") + " " + message
        historyList.add(message)
        jedis.hset(HISTORY_CACHE_NAME, arcadeName, JSON.toJSONString(historyList))
        jedis.expire(HISTORY_CACHE_NAME, cacheExpireSecond)
    }

    private fun getHistory(messageId: Long, arcadeName: String): String {
        val json: String? = jedis.hget(HISTORY_CACHE_NAME, arcadeName)
        val m = MessageBuilder()
        m.add(ComponentReply(messageId))
        if (json.isNullOrBlank()) {
            m.add(arcadeName).add(" 暂无加减卡记录。")
        } else {
            val historyList = JSON.parseArray(json, String::class.java)
            m.add(arcadeName).add(" 历史记录: ")
            for (s in historyList) {
                m.newLine().add(s)
            }
        }
        return m.toString()
    }

    private fun saveArcadeList(arcadeList: List<Arcade>) {
        jedis.setex(CACHE_NAME, cacheExpireSecond, JSON.toJSONString(arcadeList))
    }

    // 距离第二天早晨5点的秒数
    private val cacheExpireSecond: Long
        get() {
            // 距离第二天早晨5点的秒数
            val calendar = Calendar.getInstance()
            if (calendar[Calendar.HOUR_OF_DAY] >= 5) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            calendar[Calendar.HOUR_OF_DAY] = 5
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            return (calendar.timeInMillis - System.currentTimeMillis()) / 1000
        }

    @EventHandler
    fun event(event: EventGroupMessage) {
        if (!isEnable) {
            return
        }
        if (!featureService.isFeatureEnable(event.groupId, FeatureEnum.CARD_COUNTER)) {
            return
        }
        val message = event.getMessage().trim()
        if ("j" == message || "几卡" == message || "几人" == message || "几爷" == message || "几神" == message || "查卡" == message) {
            val arcadeList = getArcadeList(event.groupId, false)
                .filter { this.getArcadeGroupNumber(it).contains(event.groupId) }
            if (arcadeList.isEmpty()) {
                return
            }
            val m = MessageBuilder()
            m.add(ComponentReply(event.messageId))
            arcadeList.forEachIndexed { index, arcade ->
                if (arcade.close == 1) {
                    m.add(arcade.name).add(": 停业中")
                } else {
                    m.add(arcade.name).add(": ").add(arcade.cardNum).add("卡")
                    if (arcade.machineNum > 1 && arcade.cardNum != 0) {
                        val average = arcade.cardNum.toFloat() / arcade.machineNum
                        val floor = floor(average.toDouble()).toInt()
                        val ceil = ceil(average.toDouble()).toInt()
                        if (floor == ceil) {
                            m.add(",机均").add(floor)
                        } else {
                            //m.add(",机均").add(floor).add("-").add(ceil)
                            m.add(",机均").add(floor).add("+")
                        }
                    }
                    if (arcade.cardUpdateTime == null) {
                        m.add("(今日未更新) ")
                    } else {
                        m.add("(").add(DateUtil.format(arcade.cardUpdateTime, "HH:mm:ss")).add(") ")
                    }
                }
                if (index < arcadeList.size - 1) {
                    m.newLine()
                }
            }
            //m.add("其它: bot.crazykid.cn")
            sendGroupMsg(event, event.groupId, m.toString())
            return
        }
        if ("机厅列表" == message) {
            val arcadeList = getArcadeList(event.groupId, false)
                .filter { this.getArcadeGroupNumber(it).contains(event.groupId) }
            if (arcadeList.isEmpty()) {
                return
            }
            val m = MessageBuilder()
            m.add(ComponentReply(event.messageId)).add("机厅名称及别名如下:").newLine()
            arcadeList.forEachIndexed { index, arcade ->
                m.add(arcade.name).add(": ").add(java.lang.String.join("、", getArcadeAlias(arcade)))
                if (index < arcadeList.size - 1) {
                    m.newLine()
                }
            }
            sendGroupMsg(event, event.groupId, m.toString())
            return
        }

        // 查卡
        var arcadeName: String? = ReUtil.get(selectCardNumPattern, message, 1)
        var cardUnit = "卡"
        if (arcadeName.isNullOrBlank()) {
            arcadeName = ReUtil.get(selectCardNumPattern2, message, 1)
        } else {
            val sb = StringBuilder()
            if (ReUtil.get(selectCardNumPattern, message, 4) != null) {
                sb.append(ReUtil.get(selectCardNumPattern, message, 4))
            }
            if (ReUtil.get(selectCardNumPattern, message, 5) != null) {
                sb.append(ReUtil.get(selectCardNumPattern, message, 5))
            }
            if (sb.isNotEmpty()) {
                cardUnit = sb.toString()
            }
        }
        if (arcadeName?.isNotEmpty() == true) {
            val arcadeList = getArcadeList(event.groupId, false)
            val m = MessageBuilder()
            m.add(ComponentReply(event.messageId))
            for (arcade in arcadeList) {
                if ((arcade.name == arcadeName || getArcadeAlias(arcade).contains(arcadeName)) && (getArcadeGroupNumber(
                        arcade
                    ).contains(event.groupId) || (getArcadeGroupNumber(arcade).isEmpty() && featureService.isFeatureEnable(
                        event.groupId,
                        FeatureEnum.CARD_COUNTER_ESTER_EGG
                    )))
                ) {
                    if (arcade.close == 1) {
                        m.add(arcade.name).add(" 被标记为停业中。").newLine()
                            .add("群管可以输入\"").add(arcadeName).add("开业\"取消该标记。")
                    } else {
                        m.add(arcade.name).add("现在").add(arcade.cardNum).add(cardUnit)
                        if (arcade.machineNum!! > 1 && arcade.cardNum != 0) {
                            val average = arcade.cardNum.toFloat() / arcade.machineNum
                            val floor = floor(average.toDouble()).toInt()
                            val ceil = ceil(average.toDouble()).toInt()
                            if (floor == ceil) {
                                m.add(", 机均").add(floor).add(cardUnit).add("。").newLine()
                            } else {
                                //m.add(", 机均").add(floor).add("-").add(ceil).add(cardUnit).add("。").newLine()
                                m.add(", 机均").add(floor).add("+").add(cardUnit).add("。").newLine()
                            }
                        } else {
                            m.add("。").newLine()
                        }
                        if (arcade.cardUpdateTime == null) {
                            m.add("今日未更新。")
                        } else {
                            m.add("最后由 ").add(arcade.cardUpdateBy).add(" 更新于 ")
                                .add(DateUtil.format(arcade.cardUpdateTime, "HH:mm:ss")).add("。")
                        }
                        if (!getArcadeGroupNumber(arcade).isEmpty()) {
                            // 彩蛋不显示该提示
                            m.newLine()
                                .add("加减" + cardUnit + "数请发送\"" + arcadeName + "++\"或\"" + arcadeName + "--\"")
                        }
                    }
                    sendGroupMsg(event, event.groupId, m.toString())
                    return
                }
            }
            /*
            m.add("没这机厅! ").newLine().add("你群现在支持的机厅如下: ");
            for (Arcade arcade : arcadeList) {
                if (this.getArcadeGroupNumber(arcade).contains(event.getGroupId())) {
                    m.newLine()
                            .add(arcade.getName()).add(", 别名: ").add(String.join("、", this.getArcadeAlias(arcade)));
                }
            }
            m.newLine()
                    .add("查询本bot已接管的机厅排卡实况: http://bot.crazykid.cn/#/cardQueue");
            this.sendGroupMsg(event, event.getGroupId(), m.toString(), 1000);
             */return
        }

        // 操作卡
        val reGroup = ReUtil.getAllGroups(operateCardNumPattern, message)
        if (reGroup.isNotEmpty()) {
            val m = MessageBuilder()
            arcadeName = reGroup[1]
            val operate = reGroup[2]
            val numberStr = reGroup[3]
            val sb = StringBuilder()
            if (reGroup[4] != null) {
                sb.append(reGroup[4])
            }
            if (reGroup[5] != null) {
                sb.append(reGroup[5])
            }
            if (sb.isNotEmpty()) {
                cardUnit = sb.toString()
            }
            val helpQQ = reGroup[7]
            var helpGroupUser: GroupUser? = null
            if (helpQQ != null && event.senderId.toString() != helpQQ && event.getSelfId().toString() != helpQQ) {
                helpGroupUser = event.getGroupUser(helpQQ.toLong())
            }
            val number = numberStrToInt(numberStr)
            if (number < 0) {
                return
            }
            val arcadeList = getArcadeList(event.groupId, false)
            m.add(ComponentReply(event.messageId))
            for (arcade in arcadeList) {
                // 加卡类型 0/未知 1/加 2/减 3/设置
                var operateType = 0
                if ((arcade.name == arcadeName || getArcadeAlias(arcade).contains(arcadeName)) && (getArcadeGroupNumber(
                        arcade
                    ).contains(event.groupId) || (getArcadeGroupNumber(arcade).isEmpty() && featureService.isFeatureEnable(
                        event.groupId,
                        FeatureEnum.CARD_COUNTER_ESTER_EGG
                    )))
                ) {
                    if (arcade.close == 1) {
                        m.add(arcade.name).add(" 被标记为停业中。").newLine()
                            .add("需要群管先输入\"").add(arcadeName).add("开业\"取消该标记, 然后才能加减卡。")
                        sendGroupMsg(event, event.groupId, m.toString())
                        return
                    }

                    val operator =
                        if (event.groupSender.info.card.isNotEmpty()) event.groupSender.info.card else event.groupSender.info.nickname
                    val oldCardNum = arcade.cardNum
                    when (operate) {
                        "=", "＝" -> {
                            operateType = 3
                            setCard(arcade, number, operator)
                        }

                        "+", "＋", "加" -> {
                            if (number == 0) {
                                return
                            }
                            if (number > 10) {
                                m.add("一次不能操作多于10张卡")
                                sendGroupMsg(event, event.groupId, m.toString())
                                return
                            }
                            operateType = 1
                            addCard(arcade, number, operator)
                        }
                        else -> {
                            if (number == 0) {
                                return
                            }
                            if (arcade.cardNum < number) {
                                m.add(arcade.name).add("现在").add(arcade.cardNum).add("卡, 不够减!")
                                sendGroupMsg(event, event.groupId, m.toString())
                                return
                            }
                            operateType = 2
                            addCard(arcade, number * -1, operator)
                        }
                    }
                    saveArcadeList(arcadeList)

                    // 牛bot兼容卡数修正
                    if (event.groupId == 437189122L && (reGroup[5] == null || "卡" != reGroup[5])) {
                        if (operateType == 1 || operateType == 2) {
                            val mb = MessageBuilder()
                            mb.add(arcadeName).add(if (operateType == 1) "+" else "-").add(number).add("卡")
                            event.httpApi.sendGroupMsg(event.groupId, mb.toString())
                        }
                    }
                    if (oldCardNum != arcade.cardNum) {
                        m.add("更新成功! ")
                        if (operateType == 3) {
                            val cardChangeNum = arcade.cardNum - oldCardNum
                            m.add(if (cardChangeNum > 0) "增加了" else "减少了")
                                .add(kotlin.math.abs(cardChangeNum))
                                .add(cardUnit)
                                .add("。")
                        }
                    } else {
                        m.add("${cardUnit}数没有变化。")
                    }
                    if (operateType == 1 || operateType == 2) {
                        if (helpGroupUser != null) {
                            m.add("为 ")
                                .add(if (helpGroupUser.info.card.isNullOrBlank()) helpGroupUser.info.nickname else helpGroupUser.info.card)
                                .add(if (operateType == 1) " 加了" else " 减了").add(number).add("卡")
                            saveHistory(
                                arcade.name!!,
                                operator + " 为 " + (if (helpGroupUser.info.card.isNullOrBlank()) helpGroupUser.info.nickname else helpGroupUser.info.card) + (if (operateType == 1) " 加了" else " 减了") + number + "卡 (" + arcade.cardNum + ")"
                            )
                        } else {
                            saveHistory(
                                arcade.name!!,
                                operator + (if (operateType == 1) " 加了" else " 减了") + number + "卡 (" + arcade.cardNum + ")"
                            )
                        }
                    } else if (operateType == 3) {
                        saveHistory(arcade.name!!, "$operator 设置卡数为$number")
                    }
                    m.newLine().add(arcade.name).add("现在").add(arcade.cardNum).add(cardUnit)
                    if (arcade.machineNum > 1 && arcade.cardNum != 0) {
                        val average = arcade.cardNum.toFloat() / arcade.machineNum
                        val floor = floor(average.toDouble()).toInt()
                        val ceil = ceil(average.toDouble()).toInt()
                        if (floor == ceil) {
                            m.add(", 机均").add(floor).add(cardUnit).add("。")
                        } else {
                            //m.add(", 机均").add(floor).add("-").add(ceil).add(cardUnit)
                            m.add(", 机均").add(floor).add("+").add(cardUnit)
                        }
                    }
                    sendGroupMsg(event, event.groupId, m.toString())
                    return
                }
            }
            //m.add("没这机厅");
            //this.sendGroupMsg(event, event.getGroupId(), m.toString());
        }

        // 查地址
        arcadeName = ReUtil.get(wherePattern, message, 1)
        if (arcadeName != null) {
            val arcadeList = getArcadeList(event.groupId, false)
            val m = MessageBuilder()
            m.add(ComponentReply(event.messageId))
            for (arcade in arcadeList) {
                if ((arcade.name == arcadeName || getArcadeAlias(arcade).contains(arcadeName)) && (getArcadeGroupNumber(
                        arcade
                    ).contains(event.groupId) || (getArcadeGroupNumber(arcade).isEmpty() && featureService.isFeatureEnable(
                        event.groupId,
                        FeatureEnum.CARD_COUNTER_ESTER_EGG
                    )))
                ) {
                    m.add(arcade.address)
                    sendGroupMsg(event, event.groupId, m.toString())
                    return
                }
            }
            //m.add("没这机厅");
            //this.sendGroupMsg(event, event.getGroupId(), m.toString());
        }

        // 查历史
        arcadeName = ReUtil.get(whoPattern, message, 1)
        if (arcadeName != null) {
            val arcadeList = getArcadeList(event.groupId, false)
            val m = MessageBuilder()
            m.add(ComponentReply(event.messageId))
            for (arcade in arcadeList) {
                if ((arcade.name == arcadeName || getArcadeAlias(arcade).contains(arcadeName)) && (getArcadeGroupNumber(
                        arcade
                    ).contains(event.groupId) || (getArcadeGroupNumber(arcade).isEmpty() && featureService.isFeatureEnable(
                        event.groupId,
                        FeatureEnum.CARD_COUNTER_ESTER_EGG
                    )))
                ) {
                    sendGroupMsg(event, event.groupId, getHistory(event.messageId, arcade.name!!))
                    return
                }
            }
            //m.add("没这机厅");
            //this.sendGroupMsg(event, event.getGroupId(), m.toString());
        }

        // 标记开业停业
        arcadeName = ReUtil.get(markClosePattern, message, 1)
        if (arcadeName != null) {
            if (!event.isAdmin(event.senderId) && event.senderId != 694372459L) {
                sendGroupMsg(event, event.groupId, MessageBuilder().add("此命令仅群管可以使用").toString())
                return
            }
            val arcadeList = getArcadeList(event.groupId, false)
            val m = MessageBuilder()
            m.add(ComponentReply(event.messageId))
            for (arcade in arcadeList) {
                if ((arcade.name == arcadeName || getArcadeAlias(arcade).contains(arcadeName)) && getArcadeGroupNumber(
                        arcade
                    ).contains(event.groupId)
                ) {
                    val operate = ReUtil.get(markClosePattern, message, 2)
                    if (operate == "开业") {
                        this.setClose(arcade, 0)
                        m.add(arcade.name).add(" 已被标记为开业, 可以正常加减卡了。")
                    } else {
                        this.setClose(arcade, 1)
                        m.add(arcade.name).add(" 已被标记为停业, 群员查卡或加减卡时会收到提示。")
                    }
                    sendGroupMsg(event, event.groupId, m.toString())
                    saveArcadeList(arcadeList)
                    return
                }
            }
            //m.add("没这机厅");
            //this.sendGroupMsg(event, event.getGroupId(), m.toString());
        }
    }

    private fun numberStrToInt(numberStr: String?): Int {
        if (numberStr == null) {
            return 0
        }
        when (numberStr) {
            "零" -> return 0
            "一", "+", "＋", "-", "－" -> return 1
            "二", "两", "俩" -> return 2
            "三", "仨" -> return 3
            "四" -> return 4
            "五" -> return 5
            "六" -> return 6
            "七" -> return 7
            "八" -> return 8
            "九" -> return 9
            "十" -> return 10
            else -> {}
        }
        return try {
            numberStr.toInt()
        } catch (e: Exception) {
            -1
        }
    }

    private fun sendGroupMsg(event: EventGroupMessage, groupId: Long, message: String) {
        if (message.isBlank()) {
            return
        }
        GroupMessageCounterListener.GROUP_MAP.clear()
        GroupMessageCounterListener.GROUP_AT_MAP.clear()
        val r = Runnable {
            var sendMsg = true
            if (437189122L == groupId) {
                ThreadUtil.safeSleep(500)
                sendMsg = getMessageCountInGroup(groupId, 1875425568L) == 0 // 牛意思bot
            } else if (486156320L == groupId) {
                ThreadUtil.safeSleep(500)
                sendMsg = getMessageCountInGroup(groupId, 2568226265L) == 0 // 占星铃铃
            } else {
                ThreadUtil.safeSleep(500)
                sendMsg = getMessageAtCountInGroup(groupId, event.senderId) == 0;
            }

            if (sendMsg) {
                event.httpApi.sendGroupMsg(groupId, message)
            }
        }
        ThreadUtil.execute(r)
    }

    private fun addCard(arcade: Arcade, num: Int, updateBy: String) {
        ArcadeQueueCardUtil.addCard(arcade, num, updateBy)
        updateDatabase(arcade, arcade.cardNum, updateBy, arcade.close)
    }

    private fun setCard(arcade: Arcade, num: Int, updateBy: String) {
        ArcadeQueueCardUtil.setCard(arcade, num, updateBy)
        updateDatabase(arcade, num, updateBy, arcade.close)
    }

    private fun setClose(arcade: Arcade, close: Int) {
        ArcadeQueueCardUtil.setClose(arcade, close)
        updateDatabase(arcade, arcade.cardNum, arcade.cardUpdateBy, close)
    }

    private fun getArcadeAlias(arcade: Arcade): List<String> {
        return ArcadeQueueCardUtil.getArcadeAlias(arcade)
    }

    private fun getArcadeGroupNumber(arcade: Arcade): List<Long> {
        return ArcadeQueueCardUtil.getArcadeGroupNumber(arcade)
    }

    private fun updateDatabase(arcade: Arcade, num: Int?, updateBy: String?, close: Int?) {
        ThreadUtil.execute {
            val update = Arcade()
            update.id = arcade.id
            if (num != null) {
                update.cardNum = num
            }
            if (updateBy.isNullOrBlank()) {
                update.cardUpdateBy = updateBy
                update.cardUpdateTime = Date()
            }
            if (close != null) {
                update.close = close
            }
            arcadeDao.updateById(update)
        }
    }
}
