package cn.crazykid.qqrobot.config

import cn.hutool.db.nosql.redis.RedisDS
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import redis.clients.jedis.Jedis

/**
 * @author CrazyKid
 * @date 2022/1/29 10:54
 */
@Configuration
open class JedisConfig {
    @Bean
    open fun jedis(): Jedis {
        val jedis = RedisDS.create().jedis
        jedis.select(4)
        return jedis
    }
}
