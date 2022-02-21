package cn.crazykid.qqrobot.util

import cc.moecraft.icq.sender.IcqHttpApi
import cn.crazykid.qqrobot.entity.BatchSendGroupMessageTask
import java.util.*

/**
 * 群发信息工具类
 *
 * @author CrazyKid
 * @date 2021/3/23 15:46
 */
object BatchSendUtil {
    fun sendPrivateMessage(httpApi: IcqHttpApi, qqIds: Queue<Long>, message: String) {
        Timer().schedule(BatchSendGroupMessageTask(1, httpApi, qqIds, message), 0, 1000)
    }

    fun sendGroupMessage(httpApi: IcqHttpApi, groupIds: Queue<Long>, message: String) {
        Timer().schedule(BatchSendGroupMessageTask(2, httpApi, groupIds, message), 0, 1000)
    }
}
