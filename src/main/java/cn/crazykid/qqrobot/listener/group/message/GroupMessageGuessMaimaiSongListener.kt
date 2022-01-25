package cn.crazykid.qqrobot.listener.group.message

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.message.EventGroupMessage
import cc.moecraft.icq.sender.message.MessageBuilder
import cc.moecraft.icq.sender.message.components.ComponentAt
import cc.moecraft.icq.sender.message.components.ComponentImage
import cn.crazykid.qqrobot.entity.MaimaiMusic
import cn.crazykid.qqrobot.util.PathUtil
import cn.hutool.core.date.DateUtil
import cn.hutool.core.img.ImgUtil
import cn.hutool.core.io.FileUtil
import cn.hutool.core.lang.Console
import cn.hutool.core.thread.ThreadUtil
import cn.hutool.core.util.RandomUtil
import cn.hutool.http.HttpUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.awt.Rectangle
import java.io.File
import java.util.*

/**
 * @author CrazyKid
 * @date 2021/9/14 11:47
 */
@Component
class GroupMessageGuessMaimaiSongListener() : IcqListener() {
    @Value("\${maimaiGuessSong.enable:false}")
    private var isEnable: Boolean = false

    companion object {
        val groupList = mutableListOf<Group>()
    }

    @EventHandler
    fun event(event: EventGroupMessage) {
        if (!isEnable) {
            return
        }

        val group = this.getGroupObject(event.groupId)

        if ("maimai猜歌" == event.message) {
            if (group.running) {
                //event.httpApi.sendGroupMsg(event.groupId, "游戏进行中, 请稍后")
                return
            }
            val now = Date()
            if (group.nextCanRunTime != null && now.before(group.nextCanRunTime)) {
                event.httpApi.sendGroupMsg(event.groupId, "冷却中, 剩余 ${(group.nextCanRunTime!!.time - now.time) / 1000}s")
                return
            }

            //
            ThreadUtil.execute {
                Console.log("猜歌线程名: " + Thread.currentThread().name)
                try {
                    group.running = true
                    group.maimaiMusic = GroupMessageRandomMaimaiMusicListener.maidata.random()

                    val messageList = mutableListOf<String>()

                    if (group.maimaiMusic!!.levMas == null) {
                        messageList.add("这首歌只有DX谱面, 没有标准谱面")
                    } else if (group.maimaiMusic!!.dxLevMas == null) {
                        messageList.add("这首歌只有标准谱面, 没有DX谱面")
                    } else {
                        messageList.add("这首歌既有DX谱面, 又有标准谱面")
                    }

                    if (group.maimaiMusic!!.levReMas == null && group.maimaiMusic!!.dxLevReMas == null) {
                        messageList.add("这首歌没有白谱")
                    } else {
                        messageList.add("这首歌有白谱")
                    }

                    messageList.add("这首歌的分类是 ${group.maimaiMusic!!.category}")
                    messageList.add("这首歌的作曲是 ${group.maimaiMusic!!.artist}")
                    messageList.add("这首歌的稼动版本是 ${group.maimaiMusic!!.version}")

                    if (group.maimaiMusic!!.levMas == null) {
                        messageList.add("这首歌的DX紫谱等级是 ${group.maimaiMusic!!.dxLevMas}")
                    } else if (group.maimaiMusic!!.dxLevMas == null) {
                        messageList.add("这首歌的标准紫谱等级是 ${group.maimaiMusic!!.levMas}")
                    } else {
                        if (RandomUtil.randomInt(1, 3) == 1) {
                            messageList.add("这首歌的DX紫谱等级是 ${group.maimaiMusic!!.dxLevMas}")
                        } else {
                            messageList.add("这首歌的标准紫谱等级是 ${group.maimaiMusic!!.levMas}")
                        }
                    }

                    val path = PathUtil.getClassPath() + "temp/"
                    val coverFile: File = HttpUtil.downloadFileFromUrl(
                        "https://maimai.wahlap.com/maimai-mobile/img/Music/${group.maimaiMusic!!.imageFile}",
                        path + group.maimaiMusic!!.imageFile
                    )
                    group.coverFile = coverFile
                    val tempFileName = "${System.currentTimeMillis()}.png"
                    val tempFile = FileUtil.file("$path$tempFileName")
                    ImgUtil.cut(
                        coverFile,
                        tempFile,
                        Rectangle(RandomUtil.randomInt(20, 131), RandomUtil.randomInt(20, 131), 60, 60)
                    )

                    var messageBuilder = MessageBuilder().add("这首歌封面的一部分是:").newLine()
                        .add(ComponentImage(tempFile.toString(), true)).newLine()
                        .add("答案将在30秒后揭晓!")
                    messageList.add(messageBuilder.toString())


                    event.httpApi.sendGroupMsg(
                        event.groupId,
                        "猜歌游戏将在10秒后开始。我将在接下来每隔10秒描述这条歌曲的信息，猜歌期间可使用千雪的查歌指令, 然后通过回复歌曲名称参与猜歌。"
                    )
                    ThreadUtil.safeSleep(10000)

                    for (i in messageList.indices) {
                        if (!group.running) {
                            throw InterruptedException()
                        }
                        event.httpApi.sendGroupMsg(event.groupId, "(${i + 1}/${messageList.size}) ${messageList[i]}")
                        ThreadUtil.safeSleep(10000)
                    }
                    ThreadUtil.safeSleep(20000)

                    if (!group.running) {
                        throw InterruptedException()
                    }

                    messageBuilder = MessageBuilder().add("游戏结束! 没有人猜出来.. 答案是 ${group.maimaiMusic!!.title}").newLine()
                        .add(ComponentImage(coverFile.toString(), true))
                    event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
                } catch (e: InterruptedException) {
                    Console.log("线程停止异常捕获")
                } finally {
                    Console.log("finally块复位")
                    group.running = false
                    group.nextCanRunTime = DateUtil.offsetMinute(Date(), 5)
                }
            }
        }

        if (group.running) {
            if (group.maimaiMusic!!.title!!.equals(
                    event.message,
                    true
                ) || (event.message.length >= 4 && group.maimaiMusic!!.title!!.contains(event.message, true))
            ) {
                val messageBuilder = MessageBuilder()
                messageBuilder.add(ComponentAt(event.senderId)).add("答对了! 答案是 ").add(group.maimaiMusic!!.title)
                    .newLine()
                    .add(ComponentImage(group.coverFile.toString(), true))
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())

                // 停止线程
                /*val threads = ThreadUtil.getThreads()
                for (thread in threads) {
                    Console.log(thread.name)
                    if (thread.name == group.thread!!.name) {
                        thread.interrupt()
                        Console.log("猜中, 停止线程" + thread.name)
                    }
                }*/
                group.running = false
            }
        }
    }

    private fun getGroupObject(groupId: Long): Group {
        var group = groupList.firstOrNull { it.id == groupId }
        if (group == null) {
            group = Group()
            group.id = groupId
            groupList.add(group)
        }
        return group;
    }


}

class Group {
    var id: Long? = null
    var running: Boolean = false
    var thread: Thread? = null
    var coverFile: File? = null
    var maimaiMusic: MaimaiMusic? = null
    var nextCanRunTime: Date? = null
}

fun main() {
    val path = PathUtil.getClassPath() + "temp/";

    val coverFile: File = HttpUtil.downloadFileFromUrl(
        "https://maimai.wahlap.com/maimai-mobile/img/Music/0efe51bee252ef32.png",
        path + "0efe51bee252ef32.png"
    )
    val tempFileName = "${System.currentTimeMillis()}.jpg"
    val tempFile = FileUtil.file("$path$tempFileName")
    ImgUtil.cut(
        coverFile,
        tempFile,
        Rectangle(RandomUtil.randomInt(20, 131), RandomUtil.randomInt(20, 131), 60, 60)
    )
}
