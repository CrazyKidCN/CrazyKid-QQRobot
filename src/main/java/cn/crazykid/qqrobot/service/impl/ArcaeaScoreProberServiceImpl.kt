package cn.crazykid.qqrobot.service.impl

import cn.crazykid.qqrobot.dao.intf.ArcaeaBindDao
import cn.crazykid.qqrobot.service.IArcaeaScoreProberService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

/**
 * Arcaea查分Service实现
 *
 * @author CrazyKid
 * @date 2022/2/22 09:42
 */
class ArcaeaScoreProberServiceImpl : IArcaeaScoreProberService {
    @Autowired
    private lateinit var arcaeaBindDao: ArcaeaBindDao

    @Value("\${arcaea.botarcapi:}")
    private var api: String? = null

    override fun bindArcaeaId(qqNumber: Long, arcaeaId: Long) {
        TODO("Not yet implemented")
    }

    override fun getArcaeaIdByQQ(qqNumber: Long): Long? {
        return arcaeaBindDao.getByQQ(qqNumber)?.arcId
    }

    override fun getArcaeaInfo(arcaeaId: Long) {
        TODO("Not yet implemented")
    }

    override fun getArcaeaBest(arcaeaId: Long) {
        TODO("Not yet implemented")
    }

    override fun getArcaeaBest30(arcaeaId: Long) {
        TODO("Not yet implemented")
    }
}
