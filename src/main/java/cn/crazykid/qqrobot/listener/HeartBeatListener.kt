package cn.crazykid.qqrobot.listener

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.meta.EventMetaHeartbeat

/**
 * 心跳监听
 *
 * @author CrazyKid
 * @since 2020/12/27 14:41
 */
class HeartBeatListener : IcqListener() {
    @EventHandler
    fun onHeartBeat(event: EventMetaHeartbeat) {
        event.getBot().getLogger().debug("接收到心跳事件: {}", event)
    }
}
