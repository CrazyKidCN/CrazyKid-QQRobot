package cn.crazykid.qqrobot.listener.group.message

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.message.EventGroupMessage
import cc.moecraft.icq.sender.message.components.ComponentReply
import cn.crazykid.qqrobot.entity.MaimaiMusic
import cn.crazykid.qqrobot.service.IFeatureService
import cn.crazykid.qqrobot.service.IMaimaiMusicDataService
import cn.hutool.core.io.resource.ResourceUtil
import cn.hutool.core.util.ReUtil
import com.alibaba.fastjson.JSON
import com.google.common.collect.Lists
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.regex.Pattern

/**
 * 随个maimai歌曲
 *
 * @author CrazyKid
 * @date 2021/3/25 11:04
 */
@Component
class GroupMessageMaimaiHelperListener : IcqListener() {

    @Autowired
    private lateinit var featureService: IFeatureService

    @Autowired
    private lateinit var maimaiMusicDataService: IMaimaiMusicDataService

    companion object {
        val maidata: MutableList<MaimaiMusic> = Lists.newArrayList()
    }

    private val searchSongPattern: Pattern = Pattern.compile(
        "^查歌(\\s+)?(.*)\$",
        Pattern.CASE_INSENSITIVE
    )
    private val searchSongPattern2: Pattern = Pattern.compile(
        "^(.*)是什么歌[?？]?\$",
        Pattern.CASE_INSENSITIVE
    )

    @EventHandler
    fun event(event: EventGroupMessage) {
        // 查歌
        var searchSong: String? = ReUtil.get(searchSongPattern, event.message, 2)
        if (searchSong.isNullOrBlank()) {
            searchSong = ReUtil.get(searchSongPattern2, event.message, 1)
        }
        if (!searchSong.isNullOrBlank()) {
            val message = maimaiMusicDataService.searchSong(searchSong)
            event.httpApi.sendGroupMsg(event.groupId, ComponentReply(event.messageId).toString() + message)
            return
        }
        //event.httpApi.sendGroupMsg(event.groupId, m.toString())

    }

    init {
        val str = ResourceUtil.readUtf8Str("maidata.json")
        maidata.addAll(JSON.parseArray(str, MaimaiMusic::class.java))
    }
}

fun main() {
    val pattern: Pattern =
        Pattern.compile("^(.*)是什么歌[?？]?\$", Pattern.CASE_INSENSITIVE)
    val str = "海底谭是什么歌?"
    println(ReUtil.getAllGroups(pattern, str))
    //println(ReUtil.get(pattern, str, 2))
}
