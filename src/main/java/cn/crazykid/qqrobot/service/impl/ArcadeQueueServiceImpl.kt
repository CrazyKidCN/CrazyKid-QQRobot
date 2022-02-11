package cn.crazykid.qqrobot.service.impl

import cc.moecraft.icq.sender.message.MessageBuilder
import cn.crazykid.qqrobot.dao.intf.ArcadeDao
import cn.crazykid.qqrobot.dao.intf.ArcadeQueueHistoryDao
import cn.crazykid.qqrobot.dao.intf.ArcadeQueuePlayerDao
import cn.crazykid.qqrobot.entity.Arcade
import cn.crazykid.qqrobot.entity.ArcadeQueueHistory
import cn.crazykid.qqrobot.entity.ArcadeQueuePlayer
import cn.crazykid.qqrobot.exception.OperateFailedException
import cn.crazykid.qqrobot.service.IArcadeQueuePlayerService
import cn.crazykid.qqrobot.service.IArcadeQueueService
import cn.crazykid.qqrobot.util.ArcadeQueueCardUtil
import cn.hutool.core.lang.Console
import cn.hutool.core.thread.ThreadUtil
import com.alibaba.fastjson.JSON
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import redis.clients.jedis.Jedis
import java.util.*

/**
 * @author CrazyKid
 * @date 2022/1/9 11:00
 */
@Service
class ArcadeQueueServiceImpl : IArcadeQueueService {

    @Autowired
    private lateinit var arcadeDao: ArcadeDao

    @Autowired
    private lateinit var arcadeQueueHistoryDao: ArcadeQueueHistoryDao

    @Autowired
    private lateinit var arcadeQueuePlayerDao: ArcadeQueuePlayerDao

    @Autowired
    private lateinit var arcadeQueuePlayerService: IArcadeQueuePlayerService

    @Autowired
    private lateinit var jedis: Jedis

    companion object {
        // 格式化: 群号_机厅名
        private const val CACHE_NAME = "ArcadeQueue_%d_%s"

        // 旧的机厅排卡数缓存
        private const val ARCADE_CARD_QUEUE_CACHE_NAME = "ArcadeCardQueue_NEW"
    }

    override fun getArcadeList(groupNumber: Long, isReload: Boolean): List<Arcade> {
        Console.log("获取机厅列表...")
        val arcadeList: List<Arcade>
        val json: String? = jedis[ARCADE_CARD_QUEUE_CACHE_NAME]
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
            jedis.setex(ARCADE_CARD_QUEUE_CACHE_NAME, cacheExpireSecond, JSON.toJSONString(arcadeList))
        } else {
            Console.log("从redis中获取..")
            arcadeList = JSON.parseArray(json, Arcade::class.java)
        }

        // 排序按群号置顶
        // https://www.cnblogs.com/firstdream/p/7204067.html
        arcadeList.sortWith(
            Comparator.comparing(
                { arcade: Arcade -> ArcadeQueueCardUtil.getArcadeGroupNumber(arcade) }) { x: List<Long>, y: List<Long> ->
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

    override fun getArcade(arcadeName: String, groupNumber: Long): Arcade? {
        val arcadeList = this.getArcadeList(groupNumber, false)
        for (arcade in arcadeList) {
            val groupNumbers = ArcadeQueueCardUtil.getArcadeGroupNumber(arcade)
            val aliases = ArcadeQueueCardUtil.getArcadeAlias(arcade);
            if ((arcade.name == arcadeName || aliases.contains(arcadeName)) && groupNumbers.contains(groupNumber)) {
                return arcade
            }
        }
        return null
    }


    override fun getQueue(groupNumber: Long, arcadeName: String): MutableList<ArcadeQueuePlayer> {
        val cacheName = CACHE_NAME.format(groupNumber, arcadeName)
        val json: String? = jedis[cacheName]
        return if (json != null) {
            JSON.parseArray(json, ArcadeQueuePlayer::class.java)
        } else mutableListOf()
    }

    override fun saveQueue(
        groupNumber: Long,
        arcadeName: String,
        operateQQNumber: Long,
        queue: MutableList<ArcadeQueuePlayer>,
        saveHistory: Boolean
    ) {
        // 赋值当前队列
        queue.forEachIndexed { index, player -> player.index = index }

        val queueJson = JSON.toJSONString(queue)
        val cacheName = CACHE_NAME.format(groupNumber, arcadeName)
        jedis.set(cacheName, queueJson)

        // 保存操作历史
        if (saveHistory) {
            ThreadUtil.execute {
                val history = ArcadeQueueHistory()
                history.groupNumber = groupNumber
                history.arcadeName = arcadeName
                history.createBy = operateQQNumber.toString() // 暂时没有
                history.createQqNumber = operateQQNumber
                history.queueJson = queueJson
                arcadeQueueHistoryDao.save<ArcadeQueueHistory>(history)
            }
        }

        // 队列入库
        ThreadUtil.execute {
            arcadeQueuePlayerService.savePlayerQueueList(groupNumber, arcadeName, queue)
        }
    }

    override fun pushPlayerToQueue(
        groupNumber: Long,
        arcadeName: String,
        operateQQNumber: Long,
        player: ArcadeQueuePlayer
    ): MutableList<ArcadeQueuePlayer> {
        val queue = this.getQueue(groupNumber, arcadeName)
        queue.add(player)
        this.saveQueue(groupNumber, arcadeName, operateQQNumber, queue, true)

        return queue
    }

    override fun resetPlayerToQueueLast(
        matchMsg: String,
        qqNumber: Long,
        groupNumber: Long,
        arcadeName: String,
        resetStatus: Boolean
    ): MutableList<ArcadeQueuePlayer> {
        val queue = this.getQueue(groupNumber, arcadeName)
        // 拷贝队列, 稍后比对改动后的队列, 来增加玩家的"已保持回数"
        val oldQueue = MutableList(queue.size) { index -> queue[index] }
        var index: Int;
        if ("我" == matchMsg) {
            index = queue.indexOfFirst { it.qqNumber == qqNumber }
        } else {
            index = matchMsg.toInt() - 1
        }
        if (index == -1 || index > queue.size) {
            // 报错
            throw OperateFailedException(true, "给定的数字超出队列长度")
        }
        if (index == queue.size - 1) {
            throw OperateFailedException(true, "已经在队列末尾了, 无需重复操作")
        }
        val removePlayer = queue.removeAt(index)
        if (resetStatus) {
            removePlayer.status = 1
        }
        queue.add(removePlayer)
        // 判断增加"已保持回数
        queue.forEachIndexed { index, player ->
            if (oldQueue[index].qqNumber == player.qqNumber) {
                player.keepIndexCount++
            } else {
                player.keepIndexCount = 0
            }
        }
        this.saveQueue(groupNumber, arcadeName, qqNumber, queue, true)
        return queue
    }

    override fun markPlayerStatus(
        index: Int,
        qqNumber: Long,
        groupNumber: Long,
        arcadeName: String,
        status: Int
    ): MutableList<ArcadeQueuePlayer> {
        val queue = this.getQueue(groupNumber, arcadeName)
        queue[index].status = status
        this.saveQueue(groupNumber, arcadeName, qqNumber, queue, true)
        return queue
    }

    override fun removePlayerInQueue(
        index: Int,
        qqNumber: Long,
        groupNumber: Long,
        arcadeName: String
    ): MutableList<ArcadeQueuePlayer> {
        val queue = this.getQueue(groupNumber, arcadeName)
        queue.removeAt(index)
        this.saveQueue(groupNumber, arcadeName, qqNumber, queue, true)
        return queue
    }

    override fun swapPlayerInQueue(
        pos1: Int,
        pos2: Int,
        senderId: Long,
        groupNumber: Long,
        arcadeName: String
    ): MutableList<ArcadeQueuePlayer> {
        val queue = this.getQueue(groupNumber, arcadeName)
        val temp = queue[pos1]
        queue[pos1] = queue[pos2]
        queue[pos2] = temp
        this.saveQueue(groupNumber, arcadeName, senderId, queue, true)
        return queue
    }

    override fun recallOperate(
        qqNumber: Long,
        groupNumber: Long,
        arcadeName: String,
        isAdmin: Boolean,
        messageBuilder: MessageBuilder
    ): MutableList<ArcadeQueuePlayer> {
        val historyList = arcadeQueueHistoryDao.selectHistory(groupNumber, arcadeName, 2)
        if (historyList == null || historyList.size != 2) {
            throw OperateFailedException(true, "没有历史操作记录, 无法撤回")
        }
        if (historyList[1].createQqNumber != qqNumber && !isAdmin) {
            throw OperateFailedException(true, "您无法撤回不是您的操作, 请呼叫群管来执行此操作")
        }
        val queue = JSON.parseArray(historyList[1].queueJson, ArcadeQueuePlayer::class.java)
        this.saveQueue(groupNumber, arcadeName, qqNumber, queue, false)
        arcadeQueueHistoryDao.deleteByEntityIds(historyList[0])
        return queue
    }


    /**
     * 距离第二天早晨5点的秒数
     */
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
}
