package cn.crazykid.qqrobot.listener.group.message

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.message.EventGroupMessage
import cc.moecraft.icq.sender.message.MessageBuilder
import cc.moecraft.icq.sender.message.components.ComponentAt
import cc.moecraft.icq.sender.message.components.ComponentImage
import cc.moecraft.icq.sender.message.components.ComponentReply
import cn.crazykid.qqrobot.entity.GuessMaimaiSongConfig
import cn.crazykid.qqrobot.entity.MaimaiMusic
import cn.crazykid.qqrobot.enums.FeatureEnum
import cn.crazykid.qqrobot.service.IFeatureService
import cn.crazykid.qqrobot.service.IMaimaiMusicDataService
import cn.crazykid.qqrobot.util.PathUtil
import cn.hutool.core.date.DateUtil
import cn.hutool.core.img.ImgUtil
import cn.hutool.core.io.FileUtil
import cn.hutool.core.lang.Console
import cn.hutool.core.thread.ThreadUtil
import cn.hutool.core.util.RandomUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.http.HttpUtil
import com.alibaba.fastjson.JSON
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import redis.clients.jedis.Jedis
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

    @Autowired
    private lateinit var jedis: Jedis

    @Autowired
    private lateinit var featureService: IFeatureService

    @Autowired
    private lateinit var maimaiMusicDataService: IMaimaiMusicDataService

    companion object {
        val groupList = mutableListOf<Group>()
        val CONFIG_CACHE_NAME: String = "MaimaiGuessSong_Config"
    }

    @EventHandler
    fun event(event: EventGroupMessage) {
        if (!isEnable) {
            return
        }

        if (!featureService.isFeatureEnable(event.groupId, FeatureEnum.MAIMAI_GUESS_SONG)) {
            return
        }

        val group = this.getGroupObject(event.groupId)

        if ("??????" == event.message) {
            event.httpApi.sendGroupMsg(
                event.groupId, MessageBuilder()
                    .add(ComponentReply(event.messageId))
                    .add("??????????????????????????????????????????, ?????????\"maimai??????\"?????????").toString()
            )
            return
        }

        if (event.message.startsWith("maimai??????")) {
            if (group.running) {
                //event.httpApi.sendGroupMsg(event.groupId, "???????????????, ?????????")
                return
            }

            val now = Date()
            val msgSplit = StrUtil.splitTrim(event.message, " ")
            if (msgSplit.size == 1) {
                // ????????????????????????
                val configJson = jedis.hget(CONFIG_CACHE_NAME, event.groupId.toString())
                if (!configJson.isNullOrBlank()) {
                    val config = JSON.parseObject(configJson, GuessMaimaiSongConfig::class.java)
                    if (!config.enable!!) {
                        event.httpApi.sendGroupMsg(
                            event.groupId, MessageBuilder()
                                .add(ComponentReply(event.messageId))
                                .add("?????????????????????, ????????????, ??????????????????\"maimai?????? ??????\"").toString()
                        )
                        return
                    }
                    if (group.lastRunTime != null && now.before(
                            DateUtil.offsetSecond(
                                group.lastRunTime,
                                config.cooldown!!
                            )
                        )
                    ) {
                        event.httpApi.sendGroupMsg(
                            event.groupId, MessageBuilder()
                                .add(ComponentReply(event.messageId))
                                .add(
                                    "?????????, ?????? ${
                                        (DateUtil.offsetSecond(
                                            group.lastRunTime,
                                            config.cooldown!!
                                        ).time - now.time) / 1000
                                    }s"
                                )
                                .newLine()
                                .add("??????????????????\"maimai?????? ??????cd ??????\"??????cd").toString()
                        )
                        return
                    }
                } else {
                    // ?????????????????????
                    val config = GuessMaimaiSongConfig()
                    config.enable = true
                    config.cooldown = 300
                    jedis.hset(CONFIG_CACHE_NAME, event.groupId.toString(), JSON.toJSONString(config))
                }
            } else if (msgSplit[1] == "??????" || msgSplit[1] == "??????") {
                if (event.senderId != 694372459L && !event.isAdmin(event.senderId)) {
                    event.httpApi.sendGroupMsg(
                        event.groupId, MessageBuilder()
                            .add(ComponentReply(event.messageId))
                            .add("???bot?????????????????????????????????").toString()
                    )
                    return
                }
                // ????????????????????????
                var configJson = jedis.hget(CONFIG_CACHE_NAME, event.groupId.toString())
                if (!configJson.isNullOrBlank()) {
                    val config = JSON.parseObject(configJson, GuessMaimaiSongConfig::class.java)
                    config.enable = false
                    configJson = JSON.toJSONString(config)
                } else {
                    // ?????????????????????
                    val config = GuessMaimaiSongConfig()
                    config.enable = false
                    config.cooldown = 300
                    configJson = JSON.toJSONString(config)
                }
                // ????????????
                jedis.hset(CONFIG_CACHE_NAME, event.groupId.toString(), configJson)

                event.httpApi.sendGroupMsg(
                    event.groupId, MessageBuilder()
                        .add(ComponentReply(event.messageId))
                        .add("?????????, ?????????\"maimai?????? ??????\"?????????").toString()
                )
                return
            } else if (msgSplit[1] == "??????" || msgSplit[1] == "??????") {
                if (event.senderId != 694372459L && !event.isAdmin(event.senderId)) {
                    event.httpApi.sendGroupMsg(
                        event.groupId, MessageBuilder()
                            .add(ComponentReply(event.messageId))
                            .add("???bot?????????????????????????????????").toString()
                    )
                    return
                }
                // ????????????????????????
                var configJson = jedis.hget(CONFIG_CACHE_NAME, event.groupId.toString())
                if (!configJson.isNullOrBlank()) {
                    val config = JSON.parseObject(configJson, GuessMaimaiSongConfig::class.java)
                    config.enable = true
                    configJson = JSON.toJSONString(config)
                } else {
                    // ?????????????????????
                    val config = GuessMaimaiSongConfig()
                    config.enable = true
                    config.cooldown = 300
                    configJson = JSON.toJSONString(config)
                }
                // ????????????
                jedis.hset(CONFIG_CACHE_NAME, event.groupId.toString(), configJson)

                event.httpApi.sendGroupMsg(
                    event.groupId, MessageBuilder()
                        .add(ComponentReply(event.messageId))
                        .add("?????????, ?????????\"maimai?????? ??????\"?????????").toString()
                )
                return
            } else if (msgSplit[1] == "??????cd" || msgSplit[1] == "??????CD") {
                if (msgSplit.size < 3 || !StrUtil.isNumeric(msgSplit[2])) {
                    return
                }
                if (event.senderId != 694372459L && !event.isAdmin(event.senderId)) {
                    event.httpApi.sendGroupMsg(
                        event.groupId, MessageBuilder()
                            .add(ComponentReply(event.messageId))
                            .add("???bot?????????????????????????????????").toString()
                    )
                    return
                }
                val cooldown = msgSplit[2].toInt()
                if (cooldown < 0 || cooldown > 3600) {
                    event.httpApi.sendGroupMsg(
                        event.groupId, MessageBuilder()
                            .add(ComponentReply(event.messageId))
                            .add("cd??????????????????0-3600???").toString()
                    )
                    return
                }

                // ????????????????????????
                var configJson = jedis.hget(CONFIG_CACHE_NAME, event.groupId.toString())
                if (!configJson.isNullOrBlank()) {
                    val config = JSON.parseObject(configJson, GuessMaimaiSongConfig::class.java)
                    config.cooldown = cooldown
                    configJson = JSON.toJSONString(config)
                } else {
                    // ?????????????????????
                    val config = GuessMaimaiSongConfig()
                    config.enable = true
                    config.cooldown = cooldown
                    configJson = JSON.toJSONString(config)
                }
                // ????????????
                jedis.hset(CONFIG_CACHE_NAME, event.groupId.toString(), configJson)

                event.httpApi.sendGroupMsg(
                    event.groupId, MessageBuilder()
                        .add(ComponentReply(event.messageId))
                        .add("?????????cd??? $cooldown ???").toString()
                )
                return
            } else {
                event.httpApi.sendGroupMsg(
                    event.groupId, MessageBuilder()
                        .add(ComponentReply(event.messageId))
                        .add("????????????, ?????????????????????, ????????????????????????.").toString()
                )
                return
            }

            ThreadUtil.execute {
                println("???????????????: " + Thread.currentThread().name)
                group.thread = Thread.currentThread()
                try {
                    group.running = true
                    group.maimaiMusic = GroupMessageRandomMaimaiMusicListener.maidata.random()

                    val songAliases = maimaiMusicDataService.getAliasByMusicTitle(group.maimaiMusic!!.title!!).orEmpty()
                    val divingFishMusicData =
                        maimaiMusicDataService.getDivingFishMusicDataByTitle(group.maimaiMusic!!.title!!)

                    println("????????????: ${group.maimaiMusic!!.title}")

                    // ????????????????????????
                    if (songAliases.isNotEmpty()) {
                        println("???????????????: $songAliases")
                        group.songAliases = songAliases
                    } else {
                        println("???????????????")
                        group.songAliases = null
                    }

                    // ????????????id
                    if (divingFishMusicData != null) {
                        println("?????????????????????id: ${divingFishMusicData.id}")
                        group.musicId = divingFishMusicData.id
                    } else {
                        println("?????????????????????id")
                        group.songAliases = null
                    }

                    val messageList = mutableListOf<String>()

                    if (group.maimaiMusic!!.levMas == null) {
                        messageList.add("???????????????DX??????, ??????????????????")
                    } else if (group.maimaiMusic!!.dxLevMas == null) {
                        messageList.add("???????????????????????????, ??????DX??????")
                    } else {
                        messageList.add("???????????????DX??????, ??????????????????")
                    }

                    if (group.maimaiMusic!!.levReMas == null && group.maimaiMusic!!.dxLevReMas == null) {
                        messageList.add("?????????????????????")
                    } else {
                        messageList.add("??????????????????")
                    }

                    messageList.add("????????????????????? ${group.maimaiMusic!!.category}")

                    if (group.maimaiMusic!!.levMas == null) {
                        messageList.add("????????????DX??????????????? ${group.maimaiMusic!!.dxLevMas}")
                    } else if (group.maimaiMusic!!.dxLevMas == null) {
                        messageList.add("????????????????????????????????? ${group.maimaiMusic!!.levMas}")
                    } else {
                        if (RandomUtil.randomInt(1, 3) == 1) {
                            messageList.add("????????????DX??????????????? ${group.maimaiMusic!!.dxLevMas}")
                        } else {
                            messageList.add("????????????????????????????????? ${group.maimaiMusic!!.levMas}")
                        }
                    }

                    messageList.add("??????????????????????????? ${group.maimaiMusic!!.version}")
                    messageList.add("????????????????????? ${group.maimaiMusic!!.artist}")

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

                    var messageBuilder = MessageBuilder().add("??????????????????????????????:").newLine()
                        .add(ComponentImage(tempFile.toString(), true)).newLine()
                        .add("????????????30????????????!")
                    messageList.add(messageBuilder.toString())


                    event.httpApi.sendGroupMsg(
                        event.groupId,
                        "??????????????????10?????????????????????10????????????????????????????????????, ?????????????????????????????????????????????4??????????????????????????? ??? ?????????????????? ??? ????????????id, ???????????????\n??????????????????\"maimai?????? ??????\"???????????????, ???\"maimai?????? ??????cd ??????\"??????cd(??????300s)"
                    )
                    ThreadUtil.safeSleep(10000)

                    for (i in messageList.indices) {
                        if (!group.running || group.thread!!.name != Thread.currentThread().name) {
                            throw InterruptedException()
                        }
                        event.httpApi.sendGroupMsg(event.groupId, "(${i + 1}/${messageList.size}) ${messageList[i]}")
                        ThreadUtil.safeSleep(10000)
                    }
                    ThreadUtil.safeSleep(20000)

                    if (!group.running || group.thread!!.name != Thread.currentThread().name) {
                        throw InterruptedException()
                    }

                    messageBuilder = MessageBuilder().add("????????????! ??????????????????.. ????????? ${group.maimaiMusic!!.title}").newLine()
                        .add(ComponentImage(coverFile.toString(), true))
                    event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
                } catch (e: InterruptedException) {
                    Console.log("????????????????????????")
                } finally {
                    Console.log("finally?????????")
                    if (group.thread!!.name == Thread.currentThread().name) {
                        group.onGameEnd()
                    }
                }
            }
        }

        if (group.running) {
            if (group.maimaiMusic!!.title!!.equals(
                    event.message,
                    true
                ) || (event.message.length >= 4 && group.maimaiMusic!!.title!!.contains(event.message, true)) ||
                (!group.songAliases.isNullOrEmpty() && group.songAliases!!.contains(event.message)) ||
                (group.musicId != null && group.musicId.toString() == event.message)
            ) {
                val messageBuilder = MessageBuilder()
                messageBuilder
                    .add(ComponentReply(event.messageId))
                    .add(ComponentAt(event.senderId)).add("?????????! ????????? ").add(group.maimaiMusic!!.title)
                    .newLine()
                    .add(ComponentImage(group.coverFile.toString(), true))
                event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
                group.onGameEnd()
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
        return group
    }
}

class Group {
    var id: Long? = null
    var running: Boolean = false
    var thread: Thread? = null
    var coverFile: File? = null
    var maimaiMusic: MaimaiMusic? = null
    var songAliases: List<String>? = null
    var musicId: Long? = null
    var lastRunTime: Date? = null

    fun onGameEnd() {
        this.running = false
        this.lastRunTime = Date()
    }
}

fun main() {
    val path = PathUtil.getClassPath() + "temp/"

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
