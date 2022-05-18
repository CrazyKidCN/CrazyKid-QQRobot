package cn.crazykid.qqrobot.listener.group.message

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.message.EventGroupMessage
import cc.moecraft.icq.sender.message.MessageBuilder
import cc.moecraft.icq.sender.message.components.ComponentReply
import cn.crazykid.qqrobot.dao.intf.GroupDao
import cn.crazykid.qqrobot.entity.WahlapMaimaiArcade
import cn.crazykid.qqrobot.enum.FeatureEnum
import cn.crazykid.qqrobot.service.IFeatureService
import cn.hutool.core.util.StrUtil
import cn.hutool.http.HttpUtil
import com.alibaba.fastjson.JSON
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import redis.clients.jedis.Jedis

/**
 * 随个机厅
 *
 * @author CrazyKid
 * @date 2022/01/29 13:14
 */
@Component
class GroupMessageRandomMaimaiArcadeListener : IcqListener() {
    @Autowired
    private lateinit var groupDao: GroupDao

    @Autowired
    private lateinit var jedis: Jedis

    @Autowired
    private lateinit var featureService: IFeatureService

    companion object {
        const val WAHLAP_MAIMAI_ARCADE_LIST_CACHE_NAME = "WahlapMaimaiArcadeList"
    }

    @EventHandler
    fun event(event: EventGroupMessage) {
        if (!featureService.isFeatureEnable(event.groupId, FeatureEnum.MAIMAI_ARCADE_RANDOM)) {
            return
        }
        if (!event.message.startsWith("随个机厅")) {
            return
        }

        val splitTrim = StrUtil.splitTrim(event.message, " ", 2)

        var keyword: String? = null

        // 指令有附带参数 则参数作为关键字, 否则用群的城市属性作为关键字
        if (splitTrim.size > 1) {
            keyword = splitTrim[1]
        } else {
            val groupEntity = groupDao.selectById(event.groupId)
            if (groupEntity != null) {
                keyword = groupEntity.city
            }
        }

        // 通过华立接口获取机厅列表, 并缓存
        var arcadeList: List<WahlapMaimaiArcade>?

        var json: String? = jedis.get(WAHLAP_MAIMAI_ARCADE_LIST_CACHE_NAME)
        if (json.isNullOrBlank()) {
            json = HttpUtil.get("https://wc.wahlap.net/maidx/rest/location")
            // 缓存一天
            jedis.setex(WAHLAP_MAIMAI_ARCADE_LIST_CACHE_NAME, 60 * 60 * 24L, json)
        }
        arcadeList = JSON.parseArray(json, WahlapMaimaiArcade::class.java)

        if (!keyword.isNullOrBlank()) {
            arcadeList = arcadeList.filter { it.address!!.contains(keyword, true) }
        }

        val messageBuilder = MessageBuilder()
            .add(ComponentReply(event.messageId))

        if (arcadeList.isNullOrEmpty()) {
            messageBuilder.add("没有查询到包含该关键字的机厅")
            event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
            return
        }

        val arcade = arcadeList.random()

        messageBuilder.add("店铺名: ${arcade.arcadeName}").newLine()
            .add("机台数: ${arcade.machineCount}").newLine()
            .add("地址: ${arcade.address}")

        event.httpApi.sendGroupMsg(event.groupId, messageBuilder.toString())
    }
}
