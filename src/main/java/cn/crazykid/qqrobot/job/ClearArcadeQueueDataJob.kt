package cn.crazykid.qqrobot.job

import cn.crazykid.qqrobot.dao.intf.ArcadeQueueHistoryDao
import cn.crazykid.qqrobot.dao.intf.ArcadeQueuePlayerDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import redis.clients.jedis.Jedis

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

    @Autowired
    private lateinit var jedis: Jedis

    @Scheduled(cron = "0 0 5 * * ?")
    fun execute() {
        // 删除数据库数据
        arcadeQueuePlayerDao.deleteAll();
        arcadeQueueHistoryDao.deleteAll();

        // 删除redis数据
        val keys = jedis.keys("ArcadeQueue_")
        keys.forEach { jedis.del(it) }
    }
}
