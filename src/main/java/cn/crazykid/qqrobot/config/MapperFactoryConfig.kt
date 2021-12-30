package cn.crazykid.qqrobot.config

import cn.org.atool.fluent.mybatis.spring.MapperFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


/**
 * @author CrazyKid
 * @date 2021/12/30 09:49
 */
@Configuration
open class MapperFactoryConfig {
    // 定义fluent mybatis的MapperFactory
    @Bean
    open fun mapperFactory(): MapperFactory {
        return MapperFactory()
    }
}
