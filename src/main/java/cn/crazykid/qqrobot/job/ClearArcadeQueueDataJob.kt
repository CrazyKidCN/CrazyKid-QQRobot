package cn.crazykid.qqrobot.job

import cn.crazykid.qqrobot.dao.intf.ArcadeQueueHistoryDao
import cn.crazykid.qqrobot.dao.intf.ArcadeQueuePlayerDao
import cn.crazykid.qqrobot.wrapper.ArcadeQueueHistoryQuery
import cn.crazykid.qqrobot.wrapper.ArcadeQueuePlayerQuery
import cn.hutool.db.nosql.redis.RedisDS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 清理排卡数据 定时任务 每天凌晨5点执行
 *
 * @author CrazyKid
 * @date 2022/1/27 17:06
 */
@Component
class ClearArcadeQueueDataJob {
    @Autowired
    private lateinit var arcadeQueuePlayerDao: ArcadeQueuePlayerDao

    @Autowired
    private lateinit var arcadeQueueHistoryDao: ArcadeQueueHistoryDao

    @Scheduled(cron = "0 0 5 * * ?")
    fun execute() {
        // 删除数据库数据
        arcadeQueuePlayerDao.mapper().delete(ArcadeQueuePlayerQuery())
        arcadeQueueHistoryDao.mapper().delete(ArcadeQueueHistoryQuery())

        // 删除redis数据
        val jedis = RedisDS.create().jedis
        jedis.select(4)
        val keys = jedis.keys("ArcadeQueue_")
        keys.forEach { jedis.del(it) }
        jedis.close()
    }
}
