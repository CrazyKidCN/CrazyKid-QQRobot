package cn.crazykid.qqrobot.service

import cn.crazykid.qqrobot.entity.DivingFishMaimaiPlayerData

/**
 * @author CrazyKid
 * @date 2023/7/27 10:28
 */
interface IMaimaiProberService {
    fun queryPlayerData(type: String, value: Any): DivingFishMaimaiPlayerData?

    suspend fun b50()
}
