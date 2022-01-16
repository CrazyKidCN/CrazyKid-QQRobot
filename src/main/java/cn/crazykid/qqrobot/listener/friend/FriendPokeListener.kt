package cn.crazykid.qqrobot.listener.friend

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.notice.EventNoticeFriendPoke
import cn.hutool.core.util.RandomUtil
import org.springframework.stereotype.Component

/**
 * 好友戳一戳事件监听
 *
 * @author CrazyKid (i@crazykid.moe)
 * @since 2020/12/30 22:04
 */
@Component
class FriendPokeListener : IcqListener() {
    @EventHandler
    fun friendPoke(event: EventNoticeFriendPoke) {
        event.bot.logger.debug("好友 {} 戳了戳你", event.user.info.nickname)
        event.httpApi.sendPrivateMsg(694372459L, event.userId.toString() + "戳了bot")
        event.httpApi.sendPrivateMsg(event.userId, randomEmoticon)
    }

    private val randomEmoticon: String
        get() {
            val list: List<String> = listOf(
                "(/▽＼)",
                "(。﹏。)",
                "(✿◡‿◡)",
                "(′▽`〃)",
                "(*/ω＼*)",
                "o(*////▽////*)q",
                "(*/ω＼*)",
                "つ﹏⊂",
                "(#／。＼#)"
            )
            return list[RandomUtil.randomInt(0, list.size)]
        }
}
