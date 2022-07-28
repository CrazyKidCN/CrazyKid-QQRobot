package cn.crazykid.qqrobot.service

import cn.crazykid.qqrobot.entity.dto.GroupFeatureConfigDTO
import cn.crazykid.qqrobot.enums.FeatureEnum

/**
 * @author CrazyKid
 * @date 2022/5/4 13:16
 */
interface IFeatureService {
    fun isFeatureEnable(groupId: Long, featureEnum: FeatureEnum): Boolean

    fun groupFeatureConfigList(groupId: Long): List<GroupFeatureConfigDTO>

    fun updateGroupFeatureConfig(groupId: Long, featureId: Int, enable: Int)
}
