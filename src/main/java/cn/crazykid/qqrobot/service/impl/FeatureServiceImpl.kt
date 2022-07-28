package cn.crazykid.qqrobot.service.impl

import cn.crazykid.qqrobot.dao.intf.FeatureDao
import cn.crazykid.qqrobot.dao.intf.FeatureGroupConfigDao
import cn.crazykid.qqrobot.dao.intf.GroupDao
import cn.crazykid.qqrobot.entity.FeatureGroupConfig
import cn.crazykid.qqrobot.entity.dto.GroupFeatureConfigDTO
import cn.crazykid.qqrobot.enums.FeatureEnum
import cn.crazykid.qqrobot.exception.OperateFailedException
import cn.crazykid.qqrobot.service.IFeatureService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import redis.clients.jedis.Jedis

/**
 * @author CrazyKid
 * @date 2022/5/4 13:16
 */
@Service
class FeatureServiceImpl : IFeatureService {
    @Autowired
    private lateinit var jedis: Jedis

    @Autowired
    private lateinit var groupDao: GroupDao

    @Autowired
    private lateinit var featureDao: FeatureDao

    @Autowired
    private lateinit var featureGroupConfigDao: FeatureGroupConfigDao

    val CACHE_KEY_PREFIX = "feature:%s:%d"

    override fun isFeatureEnable(groupId: Long, featureEnum: FeatureEnum): Boolean {
        // 先读缓存看是否存在
        val cacheKey = String.format(CACHE_KEY_PREFIX, featureEnum.code, groupId)
        val cacheStr = jedis.get(cacheKey)
        if (cacheStr != null) {
            return "1" == cacheStr
        }

        // 群是否已注册
        val group = groupDao.selectById(groupId) ?: return false;

        // 群是否已配置功能
        var featureConfig = featureGroupConfigDao.selectByGroupIdAndFeatureCode(groupId, featureEnum.code)

        if (featureConfig == null) {
            // 未配置, 进行初始化
            val feature = featureDao.selectByCode(featureEnum.code) ?: return false
            featureConfig = FeatureGroupConfig()
                .setGroupId(groupId)
                .setFeatureCode(featureEnum.code)
                .setEnable(feature.defaultEnable)
            //featureGroupConfigDao.save<FeatureGroupConfigDao>(featureConfig)
        }

        // 写入缓存
        jedis.setex(cacheKey, 60 * 60L * 24, featureConfig.enable.toString())

        // 返回
        return featureConfig.enable == 1
    }

    override fun groupFeatureConfigList(groupId: Long): List<GroupFeatureConfigDTO> {
        return featureGroupConfigDao.selectGroupFeatureConfigList(groupId)
    }

    override fun updateGroupFeatureConfig(groupId: Long, featureId: Int, enable: Int) {
        val feature = featureDao.selectById(featureId) ?: throw OperateFailedException(true, "功能编号无效")
        var featureConfig = featureGroupConfigDao.selectByGroupIdAndFeatureCode(groupId, feature.code)
        if (featureConfig == null) {
            // insert record
            featureConfig = FeatureGroupConfig()
                .setGroupId(groupId)
                .setFeatureCode(feature.code)
                .setEnable(enable)
            featureGroupConfigDao.save<FeatureGroupConfigDao>(featureConfig)
        } else {
            // update record
            featureConfig.enable = enable
            featureGroupConfigDao.updateById(featureConfig)
        }

        // 写入缓存
        val cacheKey = String.format(CACHE_KEY_PREFIX, feature.code, groupId)
        jedis.setex(cacheKey, 60 * 60L * 24, enable.toString())
    }
}
