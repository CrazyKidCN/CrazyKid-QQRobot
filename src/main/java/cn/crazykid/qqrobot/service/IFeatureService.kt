package cn.crazykid.qqrobot.service

import cn.crazykid.qqrobot.enum.FeatureEnum

/**
 * @author meijinyu
 * @date 2022/5/4 13:16
 */
interface IFeatureService {
    fun isFeatureEnable(groupId: Long, featureEnum: FeatureEnum): Boolean
}
