package cn.crazykid.qqrobot.service

import cc.moecraft.icq.sender.message.MessageBuilder
import cn.crazykid.qqrobot.entity.Arcade
import cn.crazykid.qqrobot.entity.ArcadeQueuePlayer

/**
 * @author CrazyKid
 * @date 2022/1/9 11:00
 */
interface IArcadeQueueService {
    /**
     * 获取机厅列表
     */
    fun getArcadeList(groupNumber: Long, isReload: Boolean): List<Arcade>

    /**
     * 获取机厅, 为null表示获取不到, 该机厅不存在或不属于该群
     */
    fun getArcade(arcadeName: String, groupNumber: Long): Arcade?

    /**
     * 获取机厅当前队列
     */
    fun getQueue(groupNumber: Long, arcadeName: String): MutableList<ArcadeQueuePlayer>

    /**
     * 保存机厅当前队列
     */
    fun saveQueue(
        groupNumber: Long,
        arcadeName: String,
        operateQQNumber: Long,
        queue: MutableList<ArcadeQueuePlayer>,
        saveHistory: Boolean
    )

    /**
     * 将一名玩家推入队列末端
     * 返回操作完毕后的当前队列
     */
    fun pushPlayerToQueue(
        groupNumber: Long,
        arcadeName: String,
        operateQQNumber: Long,
        player: ArcadeQueuePlayer
    ): MutableList<ArcadeQueuePlayer>

    /**
     * 重置队列中某个位置的玩家到队列末尾
     * matchMsg: 队列位置, 正整数, 如1代表队列第一名, 也可以传"我"
     * qqNumber: 传操作人QQ号, 当 matchMsg 传 "我" 的时候, 将这个QQ号移到队列末端, 其它时候该参数无效
     * resetStatus: 是否重置该玩家的状态为"正常"
     * 返回操作完毕后的当前队列
     */
    fun resetPlayerToQueueLast(
        matchMsg: String,
        qqNumber: Long,
        groupNumber: Long,
        arcadeName: String,
        resetStatus: Boolean
    ): MutableList<ArcadeQueuePlayer>

    /**
     * 设置队列中某个玩家的状态
     * index为队列位置, 从0开始, 其余参数及返回值同上
     */
    fun markPlayerStatus(
        index: Int,
        qqNumber: Long,
        groupNumber: Long,
        arcadeName: String,
        status: Int
    ): MutableList<ArcadeQueuePlayer>

    /**
     * 将指定序列的玩家移出队列
     * 参数及返回值同上
     */
    fun removePlayerInQueue(
        index: Int,
        qqNumber: Long,
        groupNumber: Long,
        arcadeName: String
    ): MutableList<ArcadeQueuePlayer>

    /**
     * 队列位置交换
     */
    fun swapPlayerInQueue(
        pos1: Int,
        pos2: Int,
        senderId: Long,
        groupNumber: Long,
        arcadeName: String
    ): MutableList<ArcadeQueuePlayer>

    /**
     * 撤销操作
     * 参数: 撤销人QQ号、群号、机厅名称、撤销人是否是管理员
     * 返回撤销后队列
     */
    fun recallOperate(
        qqNumber: Long,
        groupNumber: Long,
        arcadeName: String,
        isAdmin: Boolean,
        messageBuilder: MessageBuilder
    ): MutableList<ArcadeQueuePlayer>
}
