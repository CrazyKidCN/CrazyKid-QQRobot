package cn.crazykid.qqrobot.entity

import cc.moecraft.icq.sender.IcqHttpApi
import cc.moecraft.icq.sender.message.MessageBuilder
import cn.hutool.core.util.RandomUtil
import java.util.*

/**
 * 批量群发信息 实体类
 *
 * @author CrazyKid
 * @date 2021/3/23 15:59
 */
class BatchSendGroupMessageTask(
    private var type: Int,          // 1=私聊信息 2=群聊信息
    private var httpApi: IcqHttpApi,
    private var ids: Queue<Long>,   // QQ号或者群号队列
    private var message: String
) : TimerTask() {

    override fun run() {
        if (ids.size == 0) {
            cancel()
            return
        }
        if (type == 1) {
            val randomLoopTime = RandomUtil.randomInt(ids.size.coerceIn(1, 3), ids.size.coerceIn(1, 8) + 1)
            for (i in 0..randomLoopTime) {
                ids.poll()?.let {
                    httpApi.sendPrivateMsg(it, message + this.generateRandomCode())
                    //println("${Date()} - $it: $message" + this.generateRandomCode())
                }
                Thread.sleep(RandomUtil.randomLong(1000, 2000))
            }
            Thread.sleep(RandomUtil.randomLong(2000, 4000))
        } else {
            ids.poll()?.let {
                httpApi.sendGroupMsg(it, message + this.generateRandomCode())
                //println("${Date()} - $it: $message" + this.generateRandomCode())
                Thread.sleep(RandomUtil.randomLong(2000, 4000))
            }
        }
    }

    private fun generateRandomCode(): String {
        return MessageBuilder()
            .newLine()
            .newLine()
            .add("[随机字串(无视即可): ").add(RandomUtil.randomString(10)).add("]")
            .toString()
    }
}
