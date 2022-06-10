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
import cn.crazykid.qqrobot.util.PathUtil
import cn.hutool.core.date.DateUtil
import cn.hutool.core.img.ImgUtil
import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.resource.ResourceUtil
import cn.hutool.core.lang.Console
import cn.hutool.core.text.csv.CsvUtil
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

    companion object {
        val groupList = mutableListOf<Group>()
        val songAliases: MutableMap<String, MutableList<String>> = mutableMapOf()

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

        if ("猜歌" == event.message) {
            event.httpApi.sendGroupMsg(
                event.groupId, MessageBuilder()
                    .add(ComponentReply(event.messageId))
                    .add("为了防止跟千雪的猜歌功能冲突, 请输入\"maimai猜歌\"来启动").toString()
            )
            return
        }

        if (event.message.startsWith("maimai猜歌")) {
            if (group.running) {
                //event.httpApi.sendGroupMsg(event.groupId, "游戏进行中, 请稍后")
                return
            }

            val now = Date()
            val msgSplit = StrUtil.splitTrim(event.message, " ")
            if (msgSplit.size == 1) {
                // 检查配置是否存在
                val configJson = jedis.hget(CONFIG_CACHE_NAME, event.groupId.toString())
                if (!configJson.isNullOrBlank()) {
                    val config = JSON.parseObject(configJson, GuessMaimaiSongConfig::class.java)
                    if (!config.enable!!) {
                        event.httpApi.sendGroupMsg(
                            event.groupId, MessageBuilder()
                                .add(ComponentReply(event.messageId))
                                .add("本群已禁用猜歌, 如需启用, 请让群管输入\"maimai猜歌 启用\"").toString()
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
                                    "冷却中, 剩余 ${
                                        (DateUtil.offsetSecond(
                                            group.lastRunTime,
                                            config.cooldown!!
                                        ).time - now.time) / 1000
                                    }s"
                                )
                                .newLine()
                                .add("群管可以输入\"maimai猜歌 设置cd 秒数\"设置cd").toString()
                        )
                        return
                    }
                } else {
                    // 初始化默认配置
                    val config = GuessMaimaiSongConfig()
                    config.enable = true
                    config.cooldown = 300
                    jedis.hset(CONFIG_CACHE_NAME, event.groupId.toString(), JSON.toJSONString(config))
                }
            } else if (msgSplit[1] == "禁用" || msgSplit[1] == "关闭") {
                if (event.senderId != 694372459L && !event.isAdmin(event.senderId)) {
                    event.httpApi.sendGroupMsg(
                        event.groupId, MessageBuilder()
                            .add(ComponentReply(event.messageId))
                            .add("仅bot主或群管可以进行该操作").toString()
                    )
                    return
                }
                // 检查配置是否存在
                var configJson = jedis.hget(CONFIG_CACHE_NAME, event.groupId.toString())
                if (!configJson.isNullOrBlank()) {
                    val config = JSON.parseObject(configJson, GuessMaimaiSongConfig::class.java)
                    config.enable = false
                    configJson = JSON.toJSONString(config)
                } else {
                    // 初始化默认配置
                    val config = GuessMaimaiSongConfig()
                    config.enable = false
                    config.cooldown = 300
                    configJson = JSON.toJSONString(config)
                }
                // 保存配置
                jedis.hset(CONFIG_CACHE_NAME, event.groupId.toString(), configJson)

                event.httpApi.sendGroupMsg(
                    event.groupId, MessageBuilder()
                        .add(ComponentReply(event.messageId))
                        .add("已禁用, 可输入\"maimai猜歌 启用\"来启用").toString()
                )
                return
            } else if (msgSplit[1] == "启用" || msgSplit[1] == "开启") {
                if (event.senderId != 694372459L && !event.isAdmin(event.senderId)) {
                    event.httpApi.sendGroupMsg(
                        event.groupId, MessageBuilder()
                            .add(ComponentReply(event.messageId))
                            .add("仅bot主或群管可以进行该操作").toString()
                    )
                    return
                }
                // 检查配置是否存在
                var configJson = jedis.hget(CONFIG_CACHE_NAME, event.groupId.toString())
                if (!configJson.isNullOrBlank()) {
                    val config = JSON.parseObject(configJson, GuessMaimaiSongConfig::class.java)
                    config.enable = true
                    configJson = JSON.toJSONString(config)
                } else {
                    // 初始化默认配置
                    val config = GuessMaimaiSongConfig()
                    config.enable = true
                    config.cooldown = 300
                    configJson = JSON.toJSONString(config)
                }
                // 保存配置
                jedis.hset(CONFIG_CACHE_NAME, event.groupId.toString(), configJson)

                event.httpApi.sendGroupMsg(
                    event.groupId, MessageBuilder()
                        .add(ComponentReply(event.messageId))
                        .add("已启用, 可输入\"maimai猜歌 禁用\"来禁用").toString()
                )
                return
            } else if (msgSplit[1] == "设置cd" || msgSplit[1] == "设置CD") {
                if (msgSplit.size < 3 || !StrUtil.isNumeric(msgSplit[2])) {
                    return
                }
                if (event.senderId != 694372459L && !event.isAdmin(event.senderId)) {
                    event.httpApi.sendGroupMsg(
                        event.groupId, MessageBuilder()
                            .add(ComponentReply(event.messageId))
                            .add("仅bot主或群管可以进行该操作").toString()
                    )
                    return
                }
                val cooldown = msgSplit[2].toInt()
                if (cooldown < 0 || cooldown > 3600) {
                    event.httpApi.sendGroupMsg(
                        event.groupId, MessageBuilder()
                            .add(ComponentReply(event.messageId))
                            .add("cd的取值范围为0-3600秒").toString()
                    )
                    return
                }

                // 检查配置是否存在
                var configJson = jedis.hget(CONFIG_CACHE_NAME, event.groupId.toString())
                if (!configJson.isNullOrBlank()) {
                    val config = JSON.parseObject(configJson, GuessMaimaiSongConfig::class.java)
                    config.cooldown = cooldown
                    configJson = JSON.toJSONString(config)
                } else {
                    // 初始化默认配置
                    val config = GuessMaimaiSongConfig()
                    config.enable = true
                    config.cooldown = cooldown
                    configJson = JSON.toJSONString(config)
                }
                // 保存配置
                jedis.hset(CONFIG_CACHE_NAME, event.groupId.toString(), configJson)

                event.httpApi.sendGroupMsg(
                    event.groupId, MessageBuilder()
                        .add(ComponentReply(event.messageId))
                        .add("已设置cd为 $cooldown 秒").toString()
                )
                return
            } else {
                event.httpApi.sendGroupMsg(
                    event.groupId, MessageBuilder()
                        .add(ComponentReply(event.messageId))
                        .add("参数有误, 如果要启动游戏, 请不要加任何参数.").toString()
                )
                return
            }

            ThreadUtil.execute {
                println("猜歌线程名: " + Thread.currentThread().name)
                group.thread = Thread.currentThread()
                try {
                    group.running = true
                    group.maimaiMusic = GroupMessageRandomMaimaiMusicListener.maidata.random()
                    println("要猜的歌: ${group.maimaiMusic!!.title}, 别名map长度: ${songAliases.size}")

                    // 获取这首歌的别名
                    if (songAliases.containsKey(group.maimaiMusic!!.title)) {
                        println("已找到别名: ${songAliases[group.maimaiMusic!!.title]}")
                        group.songAliases = songAliases[group.maimaiMusic!!.title]
                    } else {
                        println("未找到别名")
                        group.songAliases = null
                    }

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

                    messageList.add("这首歌的稼动版本是 ${group.maimaiMusic!!.version}")
                    messageList.add("这首歌的作曲是 ${group.maimaiMusic!!.artist}")

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
                        "猜歌游戏将在10秒后开始。我将在接下来每隔10秒描述这条歌曲的信息，猜歌期间可使用千雪的查歌指令辅助查歌, 然后通过回复歌曲名称参与猜歌。连续4个字符命中歌曲名称, 或者答出歌曲的别名, 就算答对。(不支持用千雪的歌曲id作答)\n群管可以通过\"maimai猜歌 禁用\"禁用此功能, 或\"maimai猜歌 设置cd 秒数\"设置cd(默认300s)"
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

                    messageBuilder = MessageBuilder().add("游戏结束! 没有人猜出来.. 答案是 ${group.maimaiMusic!!.title}").newLine()
                        .add(ComponentImage(coverFile.toString(), true))
                    event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
                } catch (e: InterruptedException) {
                    Console.log("线程停止异常捕获")
                } finally {
                    Console.log("finally块复位")
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
                (!group.songAliases.isNullOrEmpty() && group.songAliases!!.contains(event.message))
            ) {
                val messageBuilder = MessageBuilder()
                messageBuilder.add(ComponentAt(event.senderId)).add("答对了! 答案是 ").add(group.maimaiMusic!!.title)
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

    init {
        // 初始化别名数据, 读取csv文件
        val reader = CsvUtil.getReader()
        val data = reader.readFromStr(ResourceUtil.readUtf8Str("aliases.csv"))
        val rows = data.rows
        //遍历行
        for (csvRow in rows) {
            //\t分割, 第一个是歌曲完整名称, 后面是别名
            for (str in csvRow.rawList) {
                val split = StrUtil.splitTrim(str, "\t")
                if (split.size > 1) {
                    split.forEachIndexed { index, songName ->
                        if (index > 0) {
                            songAliases.merge(split[0], mutableListOf(songName)) { existed, replacement ->
                                run {
                                    existed.addAll(replacement)
                                    existed
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

class Group {
    var id: Long? = null
    var running: Boolean = false
    var thread: Thread? = null
    var coverFile: File? = null
    var maimaiMusic: MaimaiMusic? = null
    var songAliases: MutableList<String>? = null
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
