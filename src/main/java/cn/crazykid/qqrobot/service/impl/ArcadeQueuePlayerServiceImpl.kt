package cn.crazykid.qqrobot.service.impl

import cn.crazykid.qqrobot.dao.intf.ArcadeQueuePlayerDao
import cn.crazykid.qqrobot.entity.ArcadeQueuePlayer
import cn.crazykid.qqrobot.service.IArcadeQueuePlayerService
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * @author CrazyKid
 * @date 2022/1/9 11:00
 */
@Service
class ArcadeQueuePlayerServiceImpl : IArcadeQueuePlayerService {

    @Autowired
    private lateinit var arcadeQueuePlayerDao: ArcadeQueuePlayerDao

    override fun getPlayerQueueInfo(qqNumber: Long): ArcadeQueuePlayer? {
        return arcadeQueuePlayerDao.selectOneByQQNumber(qqNumber)
    }

    override fun savePlayerQueueInfo(player: ArcadeQueuePlayer) {
        val existPlayer = this.getPlayerQueueInfo(player.qqNumber)
        if (existPlayer == null) {
            arcadeQueuePlayerDao.save<ArcadeQueuePlayer>(player)
        } else {
            BeanUtils.copyProperties(player, existPlayer, "id")
            arcadeQueuePlayerDao.updateById(existPlayer)
        }
    }

    override fun savePlayerQueueList(groupNumber: Long, arcadeName: String, queue: MutableList<ArcadeQueuePlayer>) {
        val currentQueue = arcadeQueuePlayerDao.list(groupNumber, arcadeName)

        for (player in queue) {
            val existPlayer: ArcadeQueuePlayer? = currentQueue.firstOrNull { it.qqNumber == player.qqNumber }
            if (existPlayer == null) {
                arcadeQueuePlayerDao.save<ArcadeQueuePlayer>(player)
            } else {
                BeanUtils.copyProperties(player, existPlayer, "id")
                arcadeQueuePlayerDao.updateById(existPlayer)
                // 移除到剩下最后面的就是该删除的
                currentQueue.remove(existPlayer)
            }
        }

        if (currentQueue.isNotEmpty()) {
            val qqList = currentQueue.map { it.qqNumber }
            arcadeQueuePlayerDao.deleteByQQNumber(qqList)
        }
    }
}
