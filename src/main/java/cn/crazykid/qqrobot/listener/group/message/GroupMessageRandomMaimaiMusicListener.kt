package cn.crazykid.qqrobot.listener.group.message

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.message.EventGroupMessage
import cc.moecraft.icq.sender.message.MessageBuilder
import cc.moecraft.icq.sender.message.components.ComponentImage
import cc.moecraft.icq.sender.message.components.ComponentReply
import cn.crazykid.qqrobot.entity.MaimaiMusic
import cn.crazykid.qqrobot.enums.FeatureEnum
import cn.crazykid.qqrobot.service.IFeatureService
import cn.hutool.core.io.resource.ResourceUtil
import cn.hutool.core.util.RandomUtil
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
class GroupMessageRandomMaimaiMusicListener : IcqListener() {

    @Autowired
    private lateinit var featureService: IFeatureService

    companion object {
        val maidata: MutableList<MaimaiMusic> = Lists.newArrayList()
    }

    private val pattern: Pattern = Pattern.compile(
        "^随个(DX|SD|标准)?(流行|动漫|N站|V家|东方|原创|音击|中二|新曲)?(\\d{1,2}|\\d{1,2}[+＋])?\$",
        Pattern.CASE_INSENSITIVE
    )

    @EventHandler
    fun event(event: EventGroupMessage) {
        if (!featureService.isFeatureEnable(event.groupId, FeatureEnum.MAIMAI_SONG_RANDOM)) {
            return
        }

        var level: String? = ReUtil.get(pattern, event.message, 3)
        val type: String? = ReUtil.get(pattern, event.message, 1)
        val category: String? = ReUtil.get(pattern, event.message, 2)

        val m = MessageBuilder()
            .add(ComponentReply(event.messageId))

        if (level == null && type == null && category == null && !event.message.contains("mai什么", true)) {
            return
        }

        if (level != null) {
            level = level.replace("＋", "+")
        }

        val filterMaidata = maidata.filter { data ->
            (level == null || data.containsLevel(level)) &&
                    (type == null || data.containsType(type)) &&
                    (category == null || data.containsCategory(category))
        }

        if (filterMaidata.isEmpty()) {
            m.add("没有找到符合条件的歌曲").newLine()
                .add("使用示例: 随个(DX/SD/标准)(流行/动漫/N站/V家/东方/原创/音击/中二/新曲)(1-15)").newLine()
                .add("()代表查询条件, 为选填项")
            event.httpApi.sendGroupMsg(event.groupId, m.toString())
            return
        }

        val music = filterMaidata[RandomUtil.randomInt(0, filterMaidata.size)]

        m.add(ComponentImage("https://maimai.wahlap.com/maimai-mobile/img/Music/" + music.imageFile)).newLine()
            .add("标题: ${music.title}").newLine()
            .add("作曲: ${music.artist}").newLine()
            .add("分类: ${music.category}").newLine()
            .add("版本: ${music.version}").newLine()

        if (music.levMas != null) {
            m.newLine()
                .add("标谱等级[${music.levBas}/${music.levAdv}/${music.levExp}/${music.levMas}")
            if (music.levReMas != null) {
                m.add("/${music.levReMas}")
            }
            m.add("]")
        }

        if (music.dxLevMas != null) {
            m.newLine()
                .add("DX谱等级[${music.dxLevBas}/${music.dxLevAdv}/${music.dxLevExp}/${music.dxLevMas}")
            if (music.dxLevReMas != null) {
                m.add("/${music.dxLevReMas}")
            }
            m.add("]")
        }

        event.httpApi.sendGroupMsg(event.groupId, m.toString())
    }

    init {
        val str = ResourceUtil.readUtf8Str("maidata.json")
        maidata.addAll(JSON.parseArray(str, MaimaiMusic::class.java))
    }
}

fun main() {
    val pattern: Pattern =
        Pattern.compile("^随个(DX|SD|标准)?(流行|动漫|N站|V家|东方|原创|音击|中二)?(\\d{1,2}|\\d{1,2}[+＋])?\$", Pattern.CASE_INSENSITIVE)
    val str = "随个12+"
    println(ReUtil.getAllGroups(pattern, str))
    println(ReUtil.get(pattern, str, 3))
}
