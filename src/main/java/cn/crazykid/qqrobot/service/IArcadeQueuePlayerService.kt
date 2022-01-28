package cn.crazykid.qqrobot.service

import cn.crazykid.qqrobot.entity.ArcadeQueuePlayer

/**
 * @author CrazyKid
 * @date 2022/1/9 12:10
 */
interface IArcadeQueuePlayerService {
    /**
     * 获取玩家参与的队列 为null表示未参加
     */
    fun getPlayerQueueInfo(qqNumber: Long): ArcadeQueuePlayer?

    /**
     * 保存玩家队列信息入库
     */
    fun savePlayerQueueInfo(player: ArcadeQueuePlayer)


    fun savePlayerQueueList(groupNumber: Long, arcadeName: String, queue: MutableList<ArcadeQueuePlayer>)
}
