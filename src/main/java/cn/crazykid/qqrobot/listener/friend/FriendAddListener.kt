package cn.crazykid.qqrobot.listener.friend

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.request.EventFriendRequest
import org.springframework.stereotype.Component

/**
 * 加好友请求事件监听
 *
 * @author CrazyKid
 * @date 2021/1/14 17:15
 */
@Component
class FriendAddListener : IcqListener() {
    @EventHandler
    fun onEvent(event: EventFriendRequest) {
        // 自动通过加好友请求
        event.httpApi.setFriendAndRequest(event.flag, true)
        // 告知号主加好友信息
        event.httpApi.sendPrivateMsg(694372459L, event.userId.toString() + "添加好友, 已自动通过请求")
        try {
            Thread.sleep(2000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
            event.bot.logger.error("加好友请求事件监听, 进程睡眠时发生异常", e)
        }
        event.httpApi.sendPrivateMsg(
            event.userId,
            "欢迎添加 CrazyKid's BOT, 拉我进群请务必先加号主咨询, 勿直接拉, 直接拉你啥功能都用不了。号主QQ: 694372459"
        )
    }
}
