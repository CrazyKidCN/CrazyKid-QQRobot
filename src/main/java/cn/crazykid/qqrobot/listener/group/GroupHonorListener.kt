package cn.crazykid.qqrobot.listener.group

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.notice.EventNoticeGroupHonor
import cc.moecraft.icq.sender.message.MessageBuilder
import cc.moecraft.icq.sender.message.components.ComponentAt
import cn.crazykid.qqrobot.enums.FeatureEnum
import cn.crazykid.qqrobot.service.IFeatureService
import com.google.common.cache.CacheBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 群成员荣耀变更事件监听
 *
 * @author CrazyKid
 * @date 2021/3/31 10:08
 */
@Component
class GroupHonorListener : IcqListener() {
    // 因为该事件好像一次会触发两次, 所以需要一个缓存去过滤重复的事件推送
    private val messageSendCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build<Long, Int>()

    @Autowired
    private lateinit var featureService: IFeatureService

    @EventHandler
    fun event(event: EventNoticeGroupHonor) {
        if (!featureService.isFeatureEnable(event.groupId, FeatureEnum.DRAGON_KING_MUTE)) {
            return
        }

        if (!event.isDragonKing) {
            return
        }

        // 避免重复发信息
        if (messageSendCache.getIfPresent(event.groupId) != null) {
            return
        }
        messageSendCache.put(event.groupId, 1)

        if (Objects.equals(event.selfId, event.userId)) {
            event.httpApi.sendGroupMsg(event.groupId, "恭喜....龙王竟是我自己, 那没事了😅")
            return
        }

        val isAdmin = event.bot.groupUserManager.getUserFromID(event.selfId, event.group).isAdmin
        val isDragonKingAdmin = event.bot.groupUserManager.getUserFromID(event.userId, event.group).isAdmin

        val m = MessageBuilder()
        if (!isDragonKingAdmin && isAdmin) {
            event.httpApi.setGroupBan(event.groupId, event.userId, 1 * 60L)
            m.add("恭喜 ").add(ComponentAt(event.userId)).add(" 获得群龙王, 禁言1分钟以示庆祝")
            event.httpApi.sendGroupMsg(event.groupId, m.toString())
        } else {
            m.add(ComponentAt(event.userId)).add(" 龙王喷个水")
            event.httpApi.sendGroupMsg(event.groupId, m.toString())
        }
    }
}
