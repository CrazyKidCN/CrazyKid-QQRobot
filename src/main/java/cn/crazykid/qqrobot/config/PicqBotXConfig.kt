package cn.crazykid.qqrobot.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author CrazyKid
 * @date 2022/1/28 11:04
 */

@ConfigurationProperties(prefix = "picqbotx")
class PicqBotXConfig {
    var port: Int = 0
    lateinit var secret: String
    lateinit var accessToken: String
    lateinit var postHost: String
    var postPort: Int = 0
}
