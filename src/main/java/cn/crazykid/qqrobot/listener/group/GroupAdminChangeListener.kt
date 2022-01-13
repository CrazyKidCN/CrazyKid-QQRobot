package cn.crazykid.qqrobot.listener.group

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.notice.groupadmin.EventNoticeGroupAdminChange
import org.springframework.stereotype.Component

/**
 * 所有群管理员数量更改事件
 *
 * @author CrazyKid
 * @since 2020/12/27 17:41
 */
@Component
class GroupAdminChangeListener : IcqListener() {
    @EventHandler
    fun onGroupAdminChange(event: EventNoticeGroupAdminChange) {
        event.bot.logger.debug("群{}的群管数量变更。", event.groupId)
        // 清空那个群的用户信息缓存，以免影响群管身份获取的判断。
        event.bot.groupUserManager.groupCache.remove(event.groupId)
    }
}
