package cn.crazykid.qqrobot.listener.group.message

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.message.EventGroupMessage
import cn.hutool.core.date.DateUtil
import cn.hutool.db.nosql.redis.RedisDS
import com.google.common.cache.CacheBuilder
import com.google.common.collect.Maps
import redis.clients.jedis.Jedis
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 统计每天收到的群消息数，没别的用
 * 顺便封装了一些redis方法
 *
 * @author CrazyKid
 * @date 2021/1/19 10:33
 */
class GroupMessageCountListener : IcqListener() {
    companion object {
        /**
         * 缓存key定义, 必须包含 %s 代表日期 yyyyMMdd
         */
        const val REDIS_MESSAGE_COUNT_KEY = "QQRobot_MessageCount_%s"
        const val REDIS_AT_COUNT_KEY = "QQRobot_AtCount_%s"
        const val REDIS_POKE_COUNT_KEY = "QQRobot_PookCount_%s"
        const val REDIS_REPEAT_COUNT_KEY = "QQRobot_RepeatCount_%s"

        /**
         * 用来缓存今天的key是否已经创建, 减少redis访问量
         */
        private val REDIS_KEY_EXIST_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build<String, String>()
        private lateinit var JEDIS: Jedis

        /**
         * 在缓存中计数+1
         *
         * @param redisKeyFormat 键值, 其中要包含%s 代表日期yyyyMMdd
         */
        fun incrCountCache(redisKeyFormat: String) {
            val key = getRedisKey(redisKeyFormat)
            // 先去本地缓存看看key是否今日已创建
            var count = REDIS_KEY_EXIST_CACHE.getIfPresent(key)
            if (count == null) {
                // 本地缓存无, 去redis取值
                count = JEDIS[key]
            }
            if (count == null) {
                // key的确不存在, 创建
                // 获取到第二天凌晨1点的秒数
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                calendar[Calendar.HOUR_OF_DAY] = 1
                calendar[Calendar.MINUTE] = 0
                calendar[Calendar.SECOND] = 0
                val second = Math.toIntExact((calendar.timeInMillis - System.currentTimeMillis()) / 1000)

                // 初始化值1, 设置过期时间为第二天的凌晨1点
                JEDIS.setex(key, second.toLong(), "1")
                // 记录一下在本地缓存, 这个key今日已创建
                REDIS_KEY_EXIST_CACHE.put(key, "1")
                return
            }
            // key存在，给其增值
            JEDIS.incr(key)
        }

        fun getCountCache(date: Date): Map<String, String> {
            val map: MutableMap<String, String> = Maps.newHashMap()
            var count: String?
            count = JEDIS[getRedisKey(
                REDIS_MESSAGE_COUNT_KEY,
                date
            )]
            map["今日BOT收到群聊消息数"] = count ?: "0"
            count = JEDIS[getRedisKey(REDIS_AT_COUNT_KEY, date)]
            map["今日BOT被@次数"] = count ?: "0"
            count = JEDIS[getRedisKey(
                REDIS_POKE_COUNT_KEY,
                date
            )]
            map["今日BOT被戳次数"] = count ?: "0"
            count = JEDIS[getRedisKey(
                REDIS_REPEAT_COUNT_KEY,
                date
            )]
            map["今日BOT复读消息数"] = count ?: "0"
            return map
        }

        private fun getRedisKey(format: String): String {
            return getRedisKey(format, Date())
        }

        private fun getRedisKey(format: String, date: Date): String {
            return String.format(format, DateUtil.format(date, "yyyyMMdd"))
        }
    }

    /**
     * 构造方法, 设置jedis
     */
    init {
        JEDIS = RedisDS.create().jedis
        JEDIS.select(2)
    }

    /**
     * 接收群消息事件
     */
    @EventHandler
    fun event(event: EventGroupMessage) {
        // 接收到任何群消息, 统计数量
        incrCountCache(REDIS_MESSAGE_COUNT_KEY)
    }
}
