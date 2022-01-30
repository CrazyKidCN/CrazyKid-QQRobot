package cn.crazykid.qqrobot.listener.group

import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.message.EventGroupMessage
import cc.moecraft.icq.sender.message.MessageBuilder
import cc.moecraft.icq.sender.message.components.ComponentImage
import cn.hutool.core.util.ReUtil
import cn.hutool.http.HttpRequest
import com.alibaba.fastjson.JSON
import org.springframework.stereotype.Component
import java.text.DecimalFormat

/**
 * QQ群信息监听
 * bilibili 小程序分享 or 视频链接解析
 * 代码实现参考了 https://github.com/Tsuk1ko/cq-picsearcher-bot/blob/master/src/plugin/antiBiliMiniApp.js
 * 特此感谢原作者
 *
 * @author CrazyKid
 * @since 2020/12/27 16:18
 */
@Component
class GroupMessageBilibiliParserListener : IcqListener() {
    @EventHandler
    fun event(event: EventGroupMessage) {
        var videoUrl: String? = null

        // 如果是json消息
        if (event.getMessage().contains("[CQ:json,data=")) {
            var data: String = event.getMessage().replace("[CQ:json,data=", "")
            data = data.substring(0, data.length - 1)
            event.bot.logger.debug("json信息提取data: $data")

            // 不是 bilibili 小程序，退出
            if (!data.contains("com.tencent.miniapp_01") || !data.contains("哔哩哔哩")) {
                return
            }

            // 嘲讽小程序图片
            // P.S 现在电脑端也能打开分享了, 就没必要嘲讽了((
            /*MessageBuilder messageBuilder = new MessageBuilder()
                    .add(new ComponentAt(event.getSenderId()))
                    .add(new ComponentImage("https://i.loli.net/2020/04/27/HegAkGhcr6lbPXv.png"));
            event.getHttpApi().sendGroupMsg(event.getGroupId(), messageBuilder.toString());*/
            try {
                // 拿到小程序json
                val json = JSON.parseObject(data)
                event.bot.logger.debug("bilibili小程序json: $json")

                // 视频链接
                videoUrl = json.getJSONObject("meta").getJSONObject("detail_1").getString("qqdocurl")
                // 视频标题
                //String title = json.getJSONObject("meta").getJSONObject("detail_1").getString("desc");
            } catch (e: Exception) {
                event.bot.logger.error("读取b站视频信息发生异常", e)
            }
        } else {
            // 如果不是小程序则看看是否匹配b站链接正则，然后去拿视频信息
            videoUrl = event.getMessage()
        }
        try {
            // 从视频链接提取 av 号或者 bv 号
            val avbvMap = getAvBvFromUrl(videoUrl) ?: return
            event.httpApi.sendGroupMsg(event.groupId, getVideoInfo(avbvMap))
        } catch (e: Exception) {
            event.bot.logger.error("读取b站视频信息发生异常", e)
        }
    }

    private fun getAvBvFromUrl(url: String?): Map<String, Any>? {
        val regex = "bilibili\\.com\\/video\\/(?:[Aa][Vv]([0-9]+)|([Bb][Vv][0-9a-zA-Z]+))"
        var av: String? = ReUtil.get(regex, url, 1)
        var bv: String? = ReUtil.get(regex, url, 2)
        val map = mutableMapOf<String, Any>()
        if (!av.isNullOrBlank()) {
            map["aid"] = av
        }
        if (!bv.isNullOrBlank()) {
            map["bvid"] = bv
        }
        if (map.isNotEmpty()) {
            return map
        }

        // 没有匹配上，尝试匹配b站短连接
        val shortUrlRegex = "(b23|acg)\\.tv\\/[0-9a-zA-Z]+"
        val match: String? = ReUtil.get(shortUrlRegex, url, 0)
        if (!match.isNullOrBlank()) {
            // 拿到重定向后的链接..
            val redirectUrl = HttpRequest.get(url).execute().header("Location")
            av = ReUtil.get(regex, redirectUrl, 1)
            bv = ReUtil.get(regex, redirectUrl, 2)
            if (!av.isNullOrBlank()) {
                map["aid"] = av
            }
            if (!bv.isNullOrBlank()) {
                map["bvid"] = bv
            }
            if (map.isNotEmpty()) {
                return map
            }
        }
        // 都没匹配返回null
        return null
    }

    private fun getVideoInfo(param: Map<String, Any>?): String? {
        if (null == param || param.isEmpty()) {
            return null
        }
        val api = "https://api.bilibili.com/x/web-interface/view"
        val body = HttpRequest.get(api)
            .form(param)
            .execute()
            .body()
        val json = JSON.parseObject(body)
        val data = json.getJSONObject("data") ?: return null
        val messageBuilder = MessageBuilder()
        messageBuilder.add(ComponentImage(data.getString("pic"))).newLine()
            .add("av").add(data["aid"]).newLine()
            .add(data["title"]).newLine()
            .add("UP主: ").add(data.getJSONObject("owner")["name"]).newLine()
            .add(formatBigNumber(data.getJSONObject("stat").getIntValue("view"))).add("播放 ")
            .add(formatBigNumber(data.getJSONObject("stat").getIntValue("danmaku"))).add("弹幕").newLine()
            .add("https://www.bilibili.com/video/").add(data["bvid"])
        return messageBuilder.toString()
    }

    private fun formatBigNumber(num: Int): String {
        if (num < 10000) {
            return num.toString()
        }
        val df = DecimalFormat("#.#")
        return df.format(Integer.valueOf(num).toDouble() / 10000) + "万"
    }
}
