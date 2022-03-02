package cn.crazykid.qqrobot.service

/**
 * Arcaea查分Service
 *
 * @author CrazyKid
 * @date 2022/2/22 09:41
 */
interface IArcaeaScoreProberService {
    /**
     * 绑定 Arcaea Id
     */
    fun bindArcaeaId(qqNumber: Long, arcaeaId: Long)

    /**
     * 查询 QQ 号绑定的 ArcaeaId
     */
    fun getArcaeaIdByQQ(qqNumber: Long)

    /**
     * 根据 Arcaea Id 查询 Arcaea 信息
     */
    fun getArcaeaInfo(arcaeaId: Long)

    /**
     * 某曲目最好成绩查询
     */
    fun getArcaeaBest(arcaeaId: Long)

    /**
     * b30查询
     */
    fun getArcaeaBest30(arcaeaId: Long)
}
